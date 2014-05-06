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
package net.nokok.twitduke.core.impl.factory;

import net.nokok.twitduke.core.api.account.AccountManager;
import net.nokok.twitduke.core.impl.account.AccountManagerImpl;

/**
 * アカウントマネージャーを生成するstaticファクトリーメソッドが定義されています。
 * このクラスを使用するとアカウントマネージャーを実装の影響なしで生成することが出来ます。
 *
 */
public class AccountManagerFactory {

    /**
     * アカウントマネージャーを新たに生成します
     *
     * @return 新たに生成されたアカウントマネージャー
     */
    public static AccountManager newInstance() {
        AccountManager accountManager = new AccountManagerImpl();
        return accountManager;
    }
}
