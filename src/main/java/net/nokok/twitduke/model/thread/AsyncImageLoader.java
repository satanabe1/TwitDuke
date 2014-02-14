package net.nokok.twitduke.model.thread;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.ImageIcon;
import net.nokok.twitduke.util.ImageSizeChanger;
import net.nokok.twitduke.util.URLUtil;
import net.nokok.twitduke.view.ImageView;
import net.nokok.twitduke.view.ui.TWLabel;

public class AsyncImageLoader extends Thread {

    private LoaderThreadSyncronizer loaderThreadSyncronizer = LoaderThreadSyncronizer.getInstance();
    private       String            imageURLString;
    private final IAsyncImageLoader loader;

    public AsyncImageLoader(String imageURLString, IAsyncImageLoader loader) {
        this.imageURLString = imageURLString;
        this.loader = loader;
    }

    @Override
    public void run() {
        loader.imageLoading();
        loaderThreadSyncronizer.lock();
        final URL imageURL = URLUtil.createURL(imageURLString);
        TWLabel imageLabel = new TWLabel(ImageSizeChanger.createThumbnail(new ImageIcon(imageURL)));
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ImageView view = new ImageView(imageURL);
                view.setVisible(true);
            }
        });
        loader.imageLoaded(imageLabel);
        loaderThreadSyncronizer.unlock();
    }
}
