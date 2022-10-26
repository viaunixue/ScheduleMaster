package com.schedulemaster.app.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.schedulemaster.misc.Hash;
import com.schedulemaster.misc.LinkedList;
import com.schedulemaster.model.Lecture;
import com.schedulemaster.model.LectureTime;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TimeTableForm extends LectureView {

    private record Position(int row, int column) {
    }

    private final Hash<Position, Color> colors = new Hash<>();

    private JTable timeTable;
    private JPanel panel;
    private JScrollPane scrollPane;

    private static final String[] HEADER = {"", "월", "화", "수", "목", "금"};
    private static final String[][] RAW_DATA = {
            {"1교시", "", "", "", "", ""}, // 9
            {"2교시", "", "", "", "", ""}, // 10
            {"3교시", "", "", "", "", ""}, // 11
            {"4교시", "", "", "", "", ""}, // 12
            {"5교시", "", "", "", "", ""}, // 13
            {"6교시", "", "", "", "", ""}, // 14
            {"7교시", "", "", "", "", ""}, // 15
            {"8교시", "", "", "", "", ""}, // 16
            {"9교시", "", "", "", "", ""}, // 17
            {"10교시", "", "", "", "", ""},// 18
            {"11교시", "", "", "", "", ""},// 19
            {"12교시", "", "", "", "", ""},// 19
    };

    private static final int FIRST_CLASS_HOUR = 9;
    private static final int LAST_CLASS_HOUR = FIRST_CLASS_HOUR + RAW_DATA.length;

    private static final LectureTime.Time[] CLASS_TIME = new LectureTime.Time[RAW_DATA.length];

    private static final Color[] COLORS = {Color.decode("#FF8787"), Color.decode("#F8C4B4"), Color.decode("#E5EBB2"), Color.decode("#BCE29E"), Color.decode("#B8E8FC"), Color.decode("#B1AFFF"), Color.decode("#C8FFD4")};
    private final Hash<Lecture, Integer> lectureColors = new Hash<>();
    private final Hash<Position, String> lectureNames = new Hash<>();

    static {
        for (int time = FIRST_CLASS_HOUR, index = 0; time < LAST_CLASS_HOUR; time++, index++) {
            CLASS_TIME[index] = new LectureTime.Time(time, 0);
        }
    }

    public TimeTableForm() {
        $$$setupUI$$$();
        timeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        timeTable.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }

    @Override
    public void updateView() {
        if (lectures.getLength() < 1)
            lectureColors.clear();

        colors.clear();
        lectureNames.clear();
        int colorIndex = 0;
        for (Lecture lecture : lectures) {
            int color = colorIndex;
            if (lectureColors.hasKey(lecture))
                color = lectureColors.get(lecture);
            setClassTimes(lecture, color);

            if (!lectureColors.hasKey(lecture))
                lectureColors.put(lecture, color);
            colorIndex += 1;
        }
        panel.revalidate();
        panel.repaint();
    }

    private void setClassTimes(Lecture lecture, int colorIndex) {
        LinkedList<LectureTime.TimeSet> timeSets = lecture.time.getTimeSets();

        for (LectureTime.TimeSet timeSet : timeSets) {
            int[] rows = calcClassTimes(timeSet);
            if (rows.length > 0) {
                Position firstPosition = new Position(rows[0], timeSet.dayOfWeek() + 1);
                lectureNames.set(firstPosition, "<html><center>" + lecture.name);
            }

            for (int row : rows) {
                Position position = new Position(row, timeSet.dayOfWeek() + 1);
                colors.set(position, COLORS[colorIndex % COLORS.length]);
            }
        }
    }

    public int[] calcClassTimes(LectureTime.TimeSet timeSet) {
        int startClassIndex = 0;
        if (timeSet.start().isAfter(CLASS_TIME[CLASS_TIME.length - 1]))
            return new int[0];
        LectureTime.TimeSet compare = new LectureTime.TimeSet(timeSet.dayOfWeek(), CLASS_TIME[startClassIndex], CLASS_TIME[CLASS_TIME.length - 1]);
        while (compare.include(timeSet)) {
            startClassIndex += 1;
            compare = new LectureTime.TimeSet(timeSet.dayOfWeek(), CLASS_TIME[startClassIndex], CLASS_TIME[CLASS_TIME.length - 1]);
        }
        if (startClassIndex > 0)
            startClassIndex -= 1;

        int endClassIndex = CLASS_TIME.length - 1;
        compare = new LectureTime.TimeSet(timeSet.dayOfWeek(), CLASS_TIME[startClassIndex], CLASS_TIME[endClassIndex]);
        while (endClassIndex > startClassIndex && compare.include(timeSet)) {
            endClassIndex -= 1;
            compare = new LectureTime.TimeSet(timeSet.dayOfWeek(), CLASS_TIME[startClassIndex], CLASS_TIME[endClassIndex]);
        }

        int length = endClassIndex - startClassIndex + 1;
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            result[i] = startClassIndex + i;
        }

        return result;
    }

    public JPanel getPanel() {
        return panel;
    }

    private void createUIComponents() {
        timeTable = new JTable(RAW_DATA, HEADER);
        timeTable.getTableHeader().setReorderingAllowed(false);
        timeTable.setEnabled(false);
        timeTable.setRowHeight((int) (timeTable.getRowHeight() * 1.3));
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent component = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                component.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
                if (column == 0)
                    return component;

                Position position = new Position(row, column);
                Position nextPosition = new Position(row + 1, column);
                Color color = colors.get(position);
                String name = lectureNames.get(position);

                Color nextColor = colors.get(nextPosition);
                if (nextColor != null && nextColor.equals(color))
                    component.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
                setText(name);
                component.setBackground(color);
                return component;
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        timeTable.setDefaultRenderer(Object.class, renderer);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        scrollPane = new JScrollPane();
        panel.add(scrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        timeTable.setMinimumSize(new Dimension(30, 30));
        scrollPane.setViewportView(timeTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }

}
