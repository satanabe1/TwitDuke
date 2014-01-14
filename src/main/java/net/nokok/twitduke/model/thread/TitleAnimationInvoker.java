package net.nokok.twitduke.model.thread;

import net.nokok.twitduke.util.Threads;
import net.nokok.twitduke.view.MainView;

/**
 * タイトルのアニメーション処理をします
 */
public class TitleAnimationInvoker extends Thread implements Runnable {

    private MainView mainView;

    public TitleAnimationInvoker(MainView mainView) {
        this.mainView = mainView;
    }

    @Override
    public void run() {
        String oldTitle = mainView.getTitle();
        for (int i = 0; i < (oldTitle.length() / 2) + 1; i++) {
            mainView.setTitle(oldTitle.substring(i, oldTitle.length() - i));
            Threads.sleep(this, 30);
        }
        String welcome = "Welcome to TwitDuke";
        for (int i = 0; i < welcome.length(); i++) {
            mainView.setTitle(welcome.substring(0, i + 1));
            Threads.sleep(this, 30);
        }
        Threads.sleep(this, 100);
        for (int i = 0; i < 12; i++) {
            mainView.setTitle(welcome.substring(i, welcome.length()));
            Threads.sleep(this, 30);
        }
    }
}
