import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ishizono on 15/04/24.
 */
public class HelloPluginAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }
        String startIndexStr = Messages.showInputDialog(project, "Input start number.", "InputSequence", null);
        if (startIndexStr != null && startIndexStr.length() == 0) {
            return;
        }
        int startIndex = 0;
        try {
            startIndex = Integer.parseInt(startIndexStr);
        } catch (NumberFormatException e1) {
            return;
        }
        int digit = startIndexStr.length();

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }

        // カレントのキャレットから連番にする
        // カレントキャレットの位置を取得する
        List<Caret> allCarets = editor.getCaretModel().getAllCarets();
        HashMap map = new HashMap();
        for(Caret caret : allCarets){
            int currentCaret = caret.getOffset();
            map.put(Integer.toString(currentCaret), currentCaret);
        }
        final Document document = editor.getDocument();
        String documentText = document.getText();
        String[] textLines = documentText.split("\r\n");

        final String contents;
        // ドキュメントを読み込み、文字列を StringBuffer で組み立てる
        try {
            StringBuilder sb = new StringBuilder();
            int counter = 0;
            int editNum = startIndex;
            for (String currentLine : textLines) {
                char[] chars = currentLine.toCharArray();
                // 文字列を検索し、挿入位置を特定しその前に番号をいれていく
                for(char character : chars){
                    // zero padding
                    if(map.containsKey(Integer.toString(counter))){
                        if(Integer.toString(editNum).length() < digit){
                            int paddingCnt = digit - Integer.toString(editNum).length();
                            for(int i = 0; i < paddingCnt; i++){
                                sb.append(0);
                            }
                        }
                        sb.append(editNum);
                        sb.append(character);
                        editNum++;
                    } else {
                        sb.append(character);
                    }
                    counter++;
                }
                // zero padding
                if(map.containsKey(Integer.toString(counter))){
                    if(Integer.toString(editNum).length() < digit){
                        int paddingCnt = digit - Integer.toString(editNum).length();
                        for(int i = 0; i < paddingCnt; i++){
                            sb.append(0);
                        }
                    }
                    sb.append(editNum);
                    editNum++;
                }
                sb.append("\n");
                counter++;
            }
            contents = sb.toString();
        } catch (Exception e1) {
            return;
        }
        final Runnable readRunner = new Runnable() {
            @Override
            public void run() {
                document.setText(contents);
            }
        };
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(readRunner);
                    }
                }, "DiskRead", null);
            }
        });
    }
}
