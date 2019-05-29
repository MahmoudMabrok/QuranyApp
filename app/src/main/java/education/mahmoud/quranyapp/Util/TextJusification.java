package education.mahmoud.quranyapp.Util;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ImageSpan;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class TextJusification {

    public static void justify(final TextView textView) {

        // 标记是否已经进行过处理，因为 post 回调会在处理后继续触发
        final AtomicBoolean isJustify = new AtomicBoolean(false);

        // 处理前原始字符串
        final String textString = textView.getText().toString();

        // 用于测量文字宽度，计算分散对齐后的空格宽度
        final TextPaint textPaint = textView.getPaint();

        CharSequence textViewText = textView.getText();

        // 分散对齐后的文字
        final Spannable builder = textViewText instanceof Spannable ?
                (Spannable) textViewText :
                new SpannableString(textString);

        // 在 TextView 完成测量绘制之后执行
        textView.post(new Runnable() {
            @Override
            public void run() {

                // 判断是否已经处理过
                if (!isJustify.get()) {

                    // 获取原始布局总行数
                    final int lineCount = textView.getLineCount();
                    // 获取 textView 的宽度
                    final int textViewWidth = textView.getWidth();

                    for (int i = 0; i < lineCount; i++) {

                        // 获取行首字符位置和行尾字符位置来截取每行的文字
                        int lineStart = textView.getLayout().getLineStart(i);
                        int lineEnd = textView.getLayout().getLineEnd(i);

                        String lineString = textString.substring(lineStart, lineEnd);

                        // 最后一行不做处理
                        if (i == lineCount - 1) {
                            break;
                        }

                        // 行首行尾去掉空格以保证处理后的对齐效果
                        String trimSpaceText = lineString.trim();
                        String removeSpaceText = lineString.replaceAll(" ", "");

                        float removeSpaceWidth = textPaint.measureText(removeSpaceText);
                        float spaceCount = trimSpaceText.length() - removeSpaceText.length();

                        // 两端对齐时每个空格的重新计算的宽度
                        float eachSpaceWidth = (textViewWidth - removeSpaceWidth) / spaceCount;

                        // 两端空格需要单独处理
                        Set<Integer> endsSpace = spacePositionInEnds(lineString);
                        for (int j = 0; j < lineString.length(); j++) {
                            char c = lineString.charAt(j);

                            // 使用透明的 drawable 来填充空格部分
                            Drawable drawable = new ColorDrawable(0x00ffffff);

                            if (c == ' ') {
                                if (endsSpace.contains(j)) {
                                    // 如果是两端的空格，则宽度置为 0
                                    drawable.setBounds(0, 0, 0, 0);
                                } else {
                                    drawable.setBounds(0, 0, (int) eachSpaceWidth, 0);
                                }
                                ImageSpan span = new ImageSpan(drawable);
                                builder.setSpan(span, lineStart + j, lineStart + j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                    }

                    textView.setText(builder);
                    // 标记处理完毕
                    isJustify.set(true);
                }
            }
        });
    }

    /**
     * 返回两端的空格坐标，例如字符串 " abc  "（前面一个空格，后面两个空格）就返回 [0, 5, 6]
     */
    private static Set<Integer> spacePositionInEnds(String string) {
        Set<Integer> result = new HashSet<>();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == ' ') {
                result.add(i);
            } else {
                break;
            }
        }

        if (result.size() == string.length()) {
            return result;
        }

        for (int i = string.length() - 1; i > 0; i--) {
            char c = string.charAt(i);
            if (c == ' ') {
                result.add(i);
            } else {
                break;
            }
        }

        return result;
    }
}