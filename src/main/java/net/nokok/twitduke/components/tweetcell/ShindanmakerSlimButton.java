/*
 * The MIT License
 *
 * Copyright 2014 noko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.nokok.twitduke.components.tweetcell;

import java.awt.Dimension;
import net.nokok.twitduke.components.Dialog;
import net.nokok.twitduke.components.DialogResultListener;
import net.nokok.twitduke.components.basic.TWSlimButton;
import net.nokok.twitduke.components.customlayoutcomponents.ShindanmakerDialog;
import net.nokok.twitduke.core.factory.AsyncTwitterFactory;
import net.nokok.twitduke.core.thirdpartyservice.shindanmaker.Shindanmaker;
import net.nokok.twitduke.core.thirdpartyservice.shindanmaker.ShindanmakerImpl;
import net.nokok.twitduke.core.thirdpartyservice.shindanmaker.ShindanmakerProperty;
import twitter4j.AsyncTwitter;
import twitter4j.auth.AccessToken;

public class ShindanmakerSlimButton extends TWSlimButton {

    private static final long serialVersionUID = 2413821909257131606L;

    public ShindanmakerSlimButton(String url, AccessToken accessToken) {
        setText("診断をツイート");
        setPreferredSize(new Dimension(130, 20));
        Shindanmaker shindanmaker = new ShindanmakerImpl();
        addActionListener(e -> {
            AsyncTwitter asyncTwitter = AsyncTwitterFactory.newInstance(accessToken);
            shindanmaker.onSuccess(asyncTwitter::updateStatus);
            shindanmaker.onError(RuntimeException::new);
            ShindanmakerProperty prop = new ShindanmakerProperty();
            if ( prop.name().isPresent() ) {
                shindanmaker.sendRequest(url, prop.name().get());
                return;
            }
            Dialog<String> dialog = new ShindanmakerDialog();
            dialog.setDialogResultListener(new DialogResultListener<String>() {

                @Override
                public void cancelButtonPushed() {
                    dialog.dispose();
                }

                @Override
                public void okButtonPushed(String o) {
                    prop.writeName(o);
                    shindanmaker.sendRequest(url, o);
                    dialog.dispose();
                }
            });
            dialog.show();
        });
    }
}
