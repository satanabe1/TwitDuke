package net.nokok.twitduke.model.account;

import com.google.common.base.Splitter;
import com.google.common.primitives.Longs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import net.nokok.twitduke.main.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import twitter4j.auth.AccessToken;

public class AccessTokenManager {

    private boolean     isLoaded;
    private SimpleToken primaryUser;

    private final Log logger = LogFactory.getLog(AccessTokenManager.class);

    private static final AccessTokenManager INSTANCE = new AccessTokenManager();

    private final File                   tokenListFile   = new File(Config.Path.TOKEN_LIST_FILE);
    private final ArrayList<SimpleToken> simpleTokenList = new ArrayList<>(3);

    /**
     * AccessTokenManagerのコンストラクタです
     * このクラスはシングルトンなクラスで、他のクラスからnewを使用してのインスタンスの生成が出来ません。
     * アクセストークンのリストが存在する場合はそのリストが読み込まれます
     * アクセストークンのリストが存在しない場合、authディレクトリをカレントディレクトリ内に作成します。
     */
    private AccessTokenManager() {
        if (!isTokenListExists()) {
            logger.info("トークンリストが存在しません");

            return;
        }
        logger.debug("トークンリストが存在します");

        readTokenList();
    }

    /**
     * 初回認証時のアクセストークン関連の処理を行います。
     * アクセストークンを保存するauthディレクトリを作成し渡された
     * アクセストークンを保存します
     *
     * @param accessToken 保存するアクセストークン
     */
    public void createPrimaryAccount(AccessToken accessToken) {
        logger.debug("プライマリアカウントを作成します");

        createTokenDirectory();
        writeAccessToken(accessToken);
    }

    /**
     * カレントディレクトリにauthディレクトリを作成します
     */
    private void createTokenDirectory() {
        logger.debug("authディレクトリを作成します");

        File authDirectory = new File(Config.Path.AUTH_DIRECTORY);
        if (!authDirectory.exists()) {
            boolean result = authDirectory.mkdir();

            if (result) {
                logger.debug("authディレクトリの作成に成功しました");
            } else {
                logger.warn("authディレクトリの作成に失敗しました");
            }

        }
    }

    /**
     * AccessTokenManagerのインスタンスを返します
     *
     * @return AccessTokenManagerのインスタンス
     */
    public static AccessTokenManager getInstance() {
        return INSTANCE;
    }

    /**
     * トークンリストを読み込んでtokenListに追加します
     * トークンリストが無い場合にこのメソッドが呼び出されるとIOErrorが投げられます
     *
     * @throws java.io.IOError
     */
    void readTokenList() {
        logger.info("トークンリストを読み込みます");

        if (!isTokenListExists()) {
            String message = "トークンリストファイルが見つかりません";

            logger.error(message);

            throw new IOError(new FileNotFoundException(message));
        }
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(tokenListFile))) {
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                logger.trace(readLine);

                Iterator<String> iteratableTokenList = Splitter.on(',').trimResults().omitEmptyStrings().split(readLine).iterator();
                String userName = iteratableTokenList.next();
                long userId = Longs.tryParse(iteratableTokenList.next());
                simpleTokenList.add(new SimpleToken(userName, userId));
            }
            primaryUser = simpleTokenList.get(0);
            isLoaded = primaryUser != null;
        } catch (IOException e) {
            String message = "トークンリストの読み込み中にエラーが発生しました";
            logger.fatal(message, e);

            throw new InternalError(message);
        }
    }

    /**
     * TwitDukeのカレントディレクトリのauthディレクトリ内にトークンリストが存在するかどうか返します
     *
     * @return 存在する場合true
     */
    public boolean isTokenListExists() {
        return tokenListFile.exists();
    }

    /**
     * @return トークンリストファイルの絶対パス
     */
    public String getTokenFileListPath() {
        return tokenListFile.getPath();
    }

    /**
     * プライマリアカウントのアクセストークンファイルをディスクから読み込みます
     * 認証ファイルが生成される前にはこのメソッドは使用できません
     *
     * @return 読み込まれたAccessToken
     */
    public AccessToken readPrimaryAccount() {
        logger.info("プライマリアカウントを読み込みます");

        if (isLoaded) {
            int index = 0;
            SimpleToken user = simpleTokenList.get(index);
            AccessToken accessToken = user.getAccessToken();
            if (accessToken == null) {
                logger.debug("アクセストークンを1度もディスクから読み込んでいないようです");

                AccessToken token = readAccessToken(user.getUserId());
                simpleTokenList.set(index, new SimpleToken(token));
                return token;
            } else {
                logger.debug("キャッシュから読み込みました");

                return accessToken;
            }
        }

        logger.info("プライマリアカウントがまだ読み込まれていません");

        readTokenList();

        if (primaryUser == null) {
            String message = "認証が完了していません";
            logger.error(message);

            throw new IllegalStateException(message);
        }
        return readAccessToken(primaryUser.getUserId());
    }

    /**
     * @return プライマリアカウントのスクリーンネーム
     */
    public String getUserName() {
        if (primaryUser == null) {
            String message = "認証が完了していません";
            logger.error(message);

            throw new IllegalStateException(message);
        }
        return primaryUser.getScreenName();
    }

    /**
     * 指定されたIDを持つユーザーのアクセストークンファイルをディスクから読み込みます
     *
     * @param id 読み込むユーザーのID
     * @return 読み込まれたAccessToken
     */
    AccessToken readAccessToken(long id) {
        logger.info(id + "のトークンファイルを読み込みます");

        try (FileInputStream fileInputStream = new FileInputStream(String.format("%s%d", Config.Path.TOKENFILE_PREFIX, id));
             ObjectInputStream stream = new ObjectInputStream(fileInputStream)) {

            return (AccessToken) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            String message = "アクセストークンの読み込み中にエラーが発生しました";
            logger.error(message, e);

            throw new IOError(new IOException(message, e));
        }
    }

    /**
     * 渡されたアクセストークンをディスクに書き込みます
     * アクセストークンのスクリーンネームとIDがトークンリストにコンマ区切りで追記され、
     * 渡されたアクセストークンのオブジェクトを「token_ユーザーID」というファイル名でauthディレクトリ以下に書き込みます
     *
     * @param accessToken 書き込むアクセストークン
     * @throws java.io.IOError ファイルが見つからなかったり、ファイルがオープンできなかったりするなどの理由で処理が失敗した時にスローされます
     */
    void writeAccessToken(AccessToken accessToken) {
        logger.info(accessToken.getUserId() + "のトークンファイルを書き込みます");
        try (FileOutputStream fileOutputStream = new FileOutputStream(String.format("%s%d", Config.Path.TOKENFILE_PREFIX, accessToken.getUserId()));
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
             FileWriter writer = new FileWriter(tokenListFile, true) /*true指定で追記出来る*/) {

            writer.write(accessToken.getScreenName() + ',' + accessToken.getUserId() + '\n');
            objectOutputStream.writeObject(accessToken);
        } catch (IOException e) {
            String message = "アクセストークンの書き込み中にエラーが発生しました";
            logger.error(message, e);

            throw new IOError(new IOException(message, e));
        }
    }

    public void removeAccessToken(long id) {
        logger.info(id + "のトークンファイルを削除します");

        boolean result = new File(String.format("%s%d", Config.Path.TOKENFILE_PREFIX, id)).delete();

        if (result) {
            logger.info("トークンファイルの削除に成功しました");
        } else {
            logger.error("トークンファイルの削除に失敗しました");
        }
    }
}
