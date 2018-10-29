package org.anonymous.wallets.fullnode.ui;

import org.anonymous.wallets.fullnode.daemon.ANONClientCaller;
import org.anonymous.wallets.fullnode.daemon.ANONInstallationObserver;
import org.anonymous.wallets.fullnode.util.Log;
import org.anonymous.wallets.fullnode.util.Util;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Table to be used for transactions - specifically.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class GovernanceTable extends DataTable {

    private static final String LOCAL_MSG_SHOW_DETAILS = Util.local("LOCAL_MSG_SHOW_DETAILS");
    private static final String LOCAL_MSG_VIEW_ON_EXPLORER = Util.local("LOCAL_MSG_VIEW_ON_EXPLORER");
    private static final String LOCAL_MSG_SHOW_MEMO = Util.local("LOCAL_MSG_SHOW_MEMO");
    private static final String LOCAL_MSG_NO_MEMO = Util.local("LOCAL_MSG_NO_MEMO");
    private static final String LOCAL_MSG_NO_MEMO_TITLE = Util.local("LOCAL_MSG_NO_MEMO_TITLE");
    private static final String LOCAL_MSG_MEMO_DETAIL_1 = Util.local("LOCAL_MSG_MEMO_DETAIL_1");
    private static final String LOCAL_MSG_MEMO_DETAIL_2 = Util.local("LOCAL_MSG_MEMO_DETAIL_2");
    private static final String LOCAL_MSG_NO_MEMO_DETAIL = Util.local("LOCAL_MSG_NO_MEMO_DETAIL");
    private static final String LOCAL_MSG_MEMO = Util.local("LOCAL_MSG_MEMO");
    private static final String LOCAL_MSG_TXN_DETAILS = Util.local("LOCAL_MSG_TXN_DETAILS");
    private static final String LOCAL_MSG_TXN_DETAILS_1 = Util.local("LOCAL_MSG_TXN_DETAILS_1");
    private static final String LOCAL_MSG_TXN_NAME = Util.local("LOCAL_MSG_TXN_NAME");
    private static final String LOCAL_MSG_TXN_VALUE = Util.local("LOCAL_MSG_TXN_VALUE");
    private static final String LOCAL_MSG_TXN_CLOSE = Util.local("LOCAL_MSG_TXN_CLOSE");

    private static final String BLOCK_EXPLORER_URL = "https://explorer.anonfork.io/insight/";
    private static final String BLOCK_EXPLORER_TEST_URL = "https://texplorer.anonfork.io/insight/";

    public GovernanceTable(final Object[][] rowData, final Object[] columnNames, final JFrame parent,
                            final ANONClientCaller caller, final ANONInstallationObserver installationObserver) {
        super(rowData, columnNames);

        JMenuItem showInExplorer = new JMenuItem("View on Browser");
        popupMenu.add(showInExplorer);

        showInExplorer.addActionListener(e -> {
            if ((lastRow >= 0) && (lastColumn >= 0)) {
                try {
                    String data = GovernanceTable.this.getModel().getValueAt(lastRow, lastColumn).toString();
                    data = data.replaceAll("\"", ""); // In case it has quotes

                    Log.info("Transaction ID for block explorer is: " + data);
                    String urlPrefix = BLOCK_EXPLORER_URL;
                    if (installationObserver.isOnTestNet()) {
                        urlPrefix = BLOCK_EXPLORER_TEST_URL;
                    }
                    if (lastColumn == 3) {
                        Desktop.getDesktop().browse(new URL(urlPrefix + "/address/" + data).toURI());
                    } else if(lastColumn == 7) {
                        Desktop.getDesktop().browse(new URL(data).toURI());
                    } else {
                        Desktop.getDesktop().browse(new URL(urlPrefix + data).toURI());
                    }
                        
                } catch (Exception ex) {
                    Log.error("Unexpected error: ", ex);
                }
            } else {
                // Log perhaps
            }
        });
    } // End constructor

    private static class DetailsDialog extends JDialog {
        public DetailsDialog(JFrame parent, Map<String, String> details) throws UnsupportedEncodingException {
            
            this.setTitle(LOCAL_MSG_TXN_DETAILS);
            this.setSize(700, 310);
            this.setLocation(100, 100);
            this.setLocationRelativeTo(parent);
            this.setModal(true);
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            this.getContentPane().setLayout(new BorderLayout(0, 0));

            JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
            tempPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            JLabel infoLabel = new JLabel("<html><span style=\"font-size:0.97em;\">"
                    + LOCAL_MSG_TXN_DETAILS_1 + "</span>");
            infoLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            tempPanel.add(infoLabel, BorderLayout.CENTER);
            this.getContentPane().add(tempPanel, BorderLayout.NORTH);

            String[] columns = new String[]{LOCAL_MSG_TXN_NAME, LOCAL_MSG_TXN_VALUE};
            String[][] data = new String[details.size()][2];
            int i = 0;
            int maxPreferredWidth = 400;
            for (Entry<String, String> ent : details.entrySet()) {
                if (maxPreferredWidth < (ent.getValue().length() * 7)) {
                    maxPreferredWidth = ent.getValue().length() * 7;
                }

                data[i][0] = ent.getKey();
                data[i][1] = ent.getValue();
                i++;
            }

            

            Arrays.sort(data, new Comparator<String[]>() {
                public int compare(String[] o1, String[] o2) {
                    return o1[0].compareTo(o2[0]);
                }

                public boolean equals(Object obj) {
                    return false;
                }
            });

            DataTable table = new DataTable(data, columns);
            table.getColumnModel().getColumn(0).setPreferredWidth(200);
            table.getColumnModel().getColumn(1).setPreferredWidth(maxPreferredWidth);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            JScrollPane tablePane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            this.getContentPane().add(tablePane, BorderLayout.CENTER);

            // Lower close button
            JPanel closePanel = new JPanel();
            closePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
            JButton closeButton = new JButton(LOCAL_MSG_TXN_CLOSE);
            closePanel.add(closeButton);
            this.getContentPane().add(closePanel, BorderLayout.SOUTH);

            closeButton.addActionListener(e -> {
                DetailsDialog.this.setVisible(false);
                DetailsDialog.this.dispose();
            });
        }
    }
}