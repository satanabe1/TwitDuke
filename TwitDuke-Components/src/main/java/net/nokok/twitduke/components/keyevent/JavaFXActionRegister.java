package net.nokok.twitduke.components.keyevent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;

/**
 * JavaFX用、キーボードショートカットを設定するクラス
 * Created by wtnbsts on 2014/07/29.
 */
public class JavaFXActionRegister implements ActionRegister {

    private final Node root;
    private final Map<Node, List<EventHandler<KeyEvent>>> registry = new HashMap<>();
    private final List<Exception> errors = new ArrayList<>();

    public JavaFXActionRegister(final Node root) {
        this.root = root;
    }

    @Override
    public void registerKeyMap(final KeyMapSetting setting) {
        root.lookupAll("*")
                .stream()
                .filter(this::isResolvable)
                .forEach(node -> {
                    registerCall(node, setting);
                });
    }

    @Override
    public void unregisterAll() {
        registry.forEach((node, handlerList) -> {
            handlerList.forEach(handler -> {
                try {
                    node.removeEventHandler(KeyEvent.KEY_PRESSED, handler);
                    handlerList.remove(handler);
                } catch (Exception ex) {
                    errors.add(ex);
                }
            });
        });
    }

    @Override
    public List<Exception> getErrors() {
        return errors.stream().collect(Collectors.toList());
    }

    @Override
    public void clearErrors() {
        errors.clear();;
    }

    /**
     * JavaFXのノードにキーボードショートカットを登録する
     * 登録に成功したイベントハンドラのリストを返す
     *
     * @param node         キーボードショートカットを設定したいノード
     * @param commandClass キーボードショートカットの実体のクラス
     * @param keyBinds     キーボードショートカットのキー入力
     *
     * @return キーボードショートカットのイベントハンドラリスト
     *
     * @throws InstantiationException commandClassのインスタンス生成に失敗
     * @throws IllegalAccessException commandClassのインスタンス生成に失敗
     * @throws ClassCastException     commandClassがKeyEvent用のEventHandlerを実装していない
     */
    @SuppressWarnings("unchecked")
    private List<EventHandler<KeyEvent>> registerCommand(final Node node, final Class<?> commandClass,
                                                         final List<KeyBind> keyBinds) throws InstantiationException,
                                                                                              IllegalAccessException,
                                                                                              ClassCastException {
        List<EventHandler<KeyEvent>> added = new ArrayList<>();
        EventHandler<KeyEvent> command = (EventHandler<KeyEvent>) commandClass.newInstance();
        keyBinds.forEach(bind -> {
            EventHandler<KeyEvent> adapter = event -> {
                if ( bind.getKeyStroke().match(event) ) {
                    command.handle(event);
                }
            };
            node.addEventHandler(KeyEvent.KEY_PRESSED, adapter);
            added.add(adapter);
        });
        return added;
    }

    /**
     * JavaFXのノードと、キーボードショートカット設定情報を渡すと、そのノードにキーボードショートカットを登録する
     *
     * @param node    キーボードショートカットを設定したいノード
     * @param setting キーボードショートカット設定
     */
    private void registerCall(Node node, KeyMapSetting setting) {
        String nodeClassName = node.getClass().getCanonicalName();
        Map<String, List<KeyBind>> keyBindMap = setting.collectKeyBinds(nodeClassName).get();
        if ( keyBindMap.isEmpty() ) {
            return;
        }
        registry.putIfAbsent(node, new ArrayList<>());
        keyBindMap.forEach((id, binds) -> {
            try {
                Class<?> commandClass = Class.forName(setting.getCommandClassName(id).get());
                List<EventHandler<KeyEvent>> added = registerCommand(node, commandClass, binds);
                registry.get(node).addAll(added);
            } catch (ClassCastException | ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                errors.add(ex);
            }
        });
    }

    /**
     * オブジェクトのクラスを解決できるか判定する
     *
     * @param obj 何かのオブジェクト
     *
     * @return true : クラス名を解決できた
     *         false : クラス名を解決できない(クラスローダが違う可能性あり？)
     */
    private boolean isResolvable(Object obj) {
        return obj.getClass().getCanonicalName() != null;
    }
}
