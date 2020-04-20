package education.mahmoud.quranyapp.utils;

/**
 * created by mhashim6 (Muhammad Hashim) on 26/06/2017
 * <p>
 * <p>
 * Copyright 2017 mhashim6(Muhammad Hashim)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class RemoveTashkeel {

    //Unicode values of Tashkeel
    private final static char[] tashkeel = {(char) Integer.parseInt("064F", 16), (char) Integer.parseInt("0650", 16),
            (char) Integer.parseInt("0651", 16), (char) Integer.parseInt("0652", 16),
            (char) Integer.parseInt("064B", 16), (char) Integer.parseInt("064C", 16),
            (char) Integer.parseInt("064D", 16), (char) Integer.parseInt("064E", 16)};


    public static String removeTashkeel(String in) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int c : in.toCharArray()) {
            if (!isTashkeel((char) c))
                stringBuilder.append((char) c);
        }
        return stringBuilder.toString();


    }

    public static boolean isTashkeel(char c) {
        for (int i = 0; i < tashkeel.length; i++) {
            if (c == tashkeel[i]) {
                return true;
            }
        }
        return false;
    }

}
