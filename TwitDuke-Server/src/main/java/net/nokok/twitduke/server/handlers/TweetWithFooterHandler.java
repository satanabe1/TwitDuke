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
package net.nokok.twitduke.server.handlers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.nokok.twitduke.base.type.Retrievable;
import net.nokok.twitduke.core.factory.TAsyncTwitterFactory;
import net.nokok.twitduke.core.type.Footer;
import net.nokok.twitduke.core.type.Tweet;
import org.mortbay.jetty.Handler;
import twitter4j.AsyncTwitter;
import twitter4j.auth.AccessToken;

/**
 * フッター付きでツイートするためのハンドラです。
 * フッターを1度指定すると2回目以降のツイートはフッターパラメータを付ける必要はありません
 * 初回とフッターを更新したい時にのみフッターを指定することを推奨します
 */
public class TweetWithFooterHandler implements Retrievable<Handler> {

    private final AsyncTwitter asyncTwitter;
    private Footer footer = new Footer("");
    private final AbstractPostRequestHandler handler = new AbstractPostRequestHandler("/v1/tweetf") {

        @Override
        public void doHandle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            String footerparam = request.getParameter("footer");
            if ( footerparam != null ) {
                footer = new Footer(footerparam);
            }
            try {
                Tweet tweet = new Tweet(request.getParameter("text"), footer);
                asyncTwitter.updateStatus(tweet.toString());
                sendOK();
            } catch (NullPointerException | IllegalArgumentException e) {
                sendBadRequest();
            }
        }
    };

    public TweetWithFooterHandler(AccessToken accessToken) {
        this.asyncTwitter = TAsyncTwitterFactory.newInstance(accessToken);
    }

    public TweetWithFooterHandler(AsyncTwitter asyncTwitter) {
        this.asyncTwitter = asyncTwitter;
    }

    @Override
    public Handler get() {
        return handler;
    }
}
