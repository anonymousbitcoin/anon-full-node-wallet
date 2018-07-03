package org.btcprivate.wallets.fullnode.ui;

import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller;
import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.NetworkAndBlockchainInfo;
import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.WalletBalance;
import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.WalletCallException;
import org.btcprivate.wallets.fullnode.daemon.BTCPInstallationObserver;
import org.btcprivate.wallets.fullnode.daemon.BTCPInstallationObserver.DaemonInfo;
import org.btcprivate.wallets.fullnode.daemon.DataGatheringThread;
import org.btcprivate.wallets.fullnode.util.*;
import org.btcprivate.wallets.fullnode.util.OSUtil.OS_TYPE;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


/**
 * Dashboard ...
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
@SuppressWarnings({"deprecation"})
public class MyMasternodePanel
    extends WalletTabPanel {
  private JFrame parentFrame;
  private BTCPInstallationObserver installationObserver;
  private BTCPClientCaller clientCaller;
  private StatusUpdateErrorReporter errorReporter;
  private BackupTracker backupTracker;

  private DataGatheringThread<NetworkAndBlockchainInfo> netInfoGatheringThread = null;

  private Boolean walletIsEncrypted = null;
  private Integer blockchainPercentage = null;

  private JLabel daemonStatusLabel = null;
  private DataGatheringThread<DaemonInfo> daemonInfoGatheringThread = null;

  private JLabel walletBalanceLabel = null;
  private DataGatheringThread<WalletBalance> walletBalanceGatheringThread = null;
  private int counter = 15;
  private JTable transactionsTable = null;
  private JScrollPane transactionsTablePane = null;
  private String[][] lastMasternodesData = null;
  private DataGatheringThread<String[][]> transactionGatheringThread = null;

  private static final String small_icon_resource = "images/btcp-44.png";

  private static final String LOCAL_MSG_BTCP_WALLET_TITLE = Util.local("LOCAL_MSG_BTCP_WALLET_TITLE");
  private static final String LOCAL_MSG_BTCP_WALLET_TOOLTIP = Util.local("LOCAL_MSG_BTCP_WALLET_TOOLTIP");
  private static final String LOCAL_MSG_DAEMON_SINGLE_CONNECTION = Util.local("LOCAL_MSG_DAEMON_SINGLE_CONNECTION");
  private static final String LOCAL_MSG_DAEMON_CONNECTIONS = Util.local("LOCAL_MSG_DAEMON_CONNECTIONS");
  private static final String LOCAL_MSG_LOOKING_PEERS = Util.local("LOCAL_MSG_LOOKING_PEERS");
  private static final String LOCAL_MSG_T_BALANCE = Util.local("LOCAL_MSG_T_BALANCE");
  private static final String LOCAL_MSG_Z_BALANCE = Util.local("LOCAL_MSG_Z_BALANCE");
  private static final String LOCAL_MSG_TOTAL_BALANCE = Util.local("LOCAL_MSG_TOTAL_BALANCE");
  private static final String LOCAL_MSG_YES = Util.local("LOCAL_MSG_YES");
  private static final String LOCAL_MSG_NO = Util.local("LOCAL_MSG_NO");
  private static final String LOCAL_MSG_IMMATURE = Util.local("LOCAL_MSG_IMMATURE");
  private static final String LOCAL_MSG_IN = Util.local("LOCAL_MSG_IN");
  private static final String LOCAL_MSG_OUT = Util.local("LOCAL_MSG_OUT");
  private static final String LOCAL_MSG_MINED = Util.local("LOCAL_MSG_MINED");
  private static final String LOCAL_MSG_TXN_TYPE = Util.local("LOCAL_MSG_TXN_TYPE");
  private static final String LOCAL_MSG_TXN_DIRECTION = Util.local("LOCAL_MSG_TXN_DIRECTION");
  private static final String LOCAL_MSG_TXN_IS_CONFIRMED = Util.local("LOCAL_MSG_TXN_IS_CONFIRMED");
  private static final String LOCAL_MSG_TXN_AMOUNT = Util.local("LOCAL_MSG_TXN_AMOUNT");
  private static final String LOCAL_MSG_TXN_DATE = Util.local("LOCAL_MSG_TXN_DATE");
  private static final String LOCAL_MSG_TXN_DESTINATION = Util.local("LOCAL_MSG_TXN_DESTINATION");
  private static final String LOCAL_MSG_UNCONFIRMED_TOOLTIP = Util.local("LOCAL_MSG_UNCONFIRMED_TOOLTIP");
  private static final String LOCAL_MSG_UNCONFIRMED_TOOLTIP_B = Util.local("LOCAL_MSG_UNCONFIRMED_TOOLTIP_B");
  private static final String LOCAL_MSG_UNCONFIRMED_TOOLTIP_Z = Util.local("LOCAL_MSG_UNCONFIRMED_TOOLTIP_Z");

  private static final String LOCAL_MSG_MYMSTRNDE_ALIAS = Util.local("LOCAL_MSG_MYMSTRNDE_ALIAS");
  private static final String LOCAL_MSG_MYMSTRNDE_ADDRESS = Util.local("LOCAL_MSG_MYMSTRNDE_ADDRESS");
  private static final String LOCAL_MSG_MYMSTRNDE_PRIVATEKEY = Util.local("LOCAL_MSG_MYMSTRNDE_PRIVATEKEY");
  private static final String LOCAL_MSG_MYMSTRNDE_TXHASH = Util.local("LOCAL_MSG_MYMSTRNDE_TXHASH");
  private static final String LOCAL_MSG_MYMSTRNDE_OUTPUTINDEX = Util.local("LOCAL_MSG_MYMSTRNDE_OUTPUTINDEX");
  private static final String LOCAL_MSG_MYMSTRNDE_STATUS = Util.local("LOCAL_MSG_MYMSTRNDE_STATUS");

  private static final String LOCAL_MSG_SYNC = Util.local("LOCAL_MSG_SYNC");
  private static final String LOCAL_MSG_BLOCK = Util.local("LOCAL_MSG_BLOCK");

  private static final String daemon_txn_receive = "receive";
  private static final String daemon_txn_send = "send";
  private static final String daemon_txn_mined = "generate";
  private static final String daemon_txn_unconfirmed = "immature";


  public MyMasternodePanel(JFrame parentFrame,
                        BTCPInstallationObserver installationObserver,
                        BTCPClientCaller clientCaller,
                        StatusUpdateErrorReporter errorReporter,
                        BackupTracker backupTracker)
      throws IOException, InterruptedException, WalletCallException {
    this.parentFrame = parentFrame;
    this.installationObserver = installationObserver;
    this.clientCaller = clientCaller;
    this.errorReporter = errorReporter;
    this.backupTracker = backupTracker;

    this.timers = new ArrayList<>();
    this.threads = new ArrayList<>();

    // Build content
    JPanel dashboard = this;
    dashboard.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    dashboard.setLayout(new BorderLayout(0, 0));

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
    buttonPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    
    JButton resetMnsyncButton = new JButton("Reset Sync");
    // buttonPanel.add(resetMnsyncButton);

    // JButton startAliasButton = new JButton(LOCAL_MENU_NEW_B_ADDRESS);
    JButton startAliasButton = new JButton("Start Alias");
    buttonPanel.add(startAliasButton);

    // JButton startAllButton = new JButton(LOCAL_MENU_NEW_Z_ADDRESS);
    JButton startAllButton = new JButton("Start All");
    buttonPanel.add(startAllButton);

    // JButton startMissingButton = new JButton(LOCAL_MENU_REFRESH);
    JButton startMissingButton = new JButton("Start Missing");
    buttonPanel.add(startMissingButton);

    // JButton updateTableButton = new JButton(LOCAL_MENU_REFRESH);
    JButton updateTableButton = new JButton("Update Table");
    buttonPanel.add(updateTableButton);

    JLabel updateLabelStart = new JLabel("Table is updated every 5 seconds");
    buttonPanel.add(updateLabelStart);

    

    dashboard.add(buttonPanel, BorderLayout.SOUTH);

    startAliasButton.addActionListener(e -> {
      try{
        String[] aliases = MyMasternodePanel.this.clientCaller.getMyAliases();
        Log.info(aliases.toString());
        String name = (String) JOptionPane.showInputDialog(MyMasternodePanel.this,
        "Please select from your list of aliases, \nprovided in the configuration file.",
        "Start Masternode By Alias",
        JOptionPane.PLAIN_MESSAGE,
        null,
        aliases,
        aliases[0]);

        if(name == null || "".equals(name)){
          return;
        }
        String response = this.clientCaller.startMasternodeByAlias(name);
        Log.info("response: " + response.toString());
        startAliasButton.setText("Starting " + name);


      }catch (Exception ex) {
        Log.error("Error in startAlias:" + ex);
      }
      counter = 5;
    });

    ActionListener updateTimer = e -> {
      try
      {
        if (counter == 0){
          startAllButton.setText("Start All");
          startAliasButton.setText("Start Alias");
          startMissingButton.setText("Start Missing");
          updateTableButton.setText("Update Table");
          resetMnsyncButton.setText("Reset Sync");
          // counter = 5;
        }
        counter--;
      } catch (Exception ex) {
        Log.error("Error in updateTimer:" + ex);
      }
    };

    Timer xy = new Timer(1000, updateTimer);
    xy.start();
    this.timers.add(xy);

    updateTableButton.addActionListener(e -> {
      try{
        MyMasternodePanel.this.updateMasternodesTable();
        Log.info("Updating masternode status");

        Log.info("----------------------------------------------------");
        updateTableButton.setText("Updating!");

      } catch (Exception ex)
      {
        Log.error("Eror in updateTableButton: " + ex);
      }
      counter = 5;
    });

    startAllButton.addActionListener(e -> {
      try{
        String response = this.clientCaller.startAllMasternodes();
        Log.info(response.toString());
        startAllButton.setText("Starting...");

      }catch (Exception ex){
        Log.error("Error in startAllButton: " + ex);
      }
      counter = 5;
    });

    startMissingButton.addActionListener(e -> {
      try
      {
        String response = this.clientCaller.startMissingMasternodes();
        Log.info(response.toString());
        startMissingButton.setText("Starting...");
      } catch (Exception ex){
        startMissingButton.setText("Not synced!");
        Log.error("Error in startMissingButton: " + ex);
      }
      counter = 5;
    });

    resetMnsyncButton.addActionListener(e -> {
      try{
        String response = this.clientCaller.executeMnsyncReset();
        resetMnsyncButton.setText(response);

      }catch (Exception ex){
        resetMnsyncButton.setText("failed");
        Log.error("Error in resetMnsyncButton: " + ex);
      }
    });

    lastMasternodesData = getMasternodeListFromRPC();
    dashboard.add(daemonStatusLabel = new JLabel(), BorderLayout.NORTH);
    dashboard.add(resetMnsyncButton);
    dashboard.add(transactionsTablePane = new JScrollPane(
            transactionsTable = this.createMasternodesTable(lastMasternodesData)),BorderLayout.CENTER);


    // Thread and timer to update the transactions table
    this.transactionGatheringThread = new DataGatheringThread<>(
        () -> {
          long start = System.currentTimeMillis();
          String[][] data = MyMasternodePanel.this.getMasternodeListFromRPC();
          long end = System.currentTimeMillis();
          Log.info("Gathering MY MASTERNODES: " + (end - start) + "ms.");

          return data;
        },
        this.errorReporter, 20000);
    this.threads.add(this.transactionGatheringThread);

    ActionListener alMasternodes = e -> {
      try {
        MyMasternodePanel.this.updateMasternodesTable();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        MyMasternodePanel.this.errorReporter.reportError(ex);
      }
    };
    Timer t = new Timer(5000, alMasternodes);
    t.start();
    this.timers.add(t);

    ActionListener alDeamonStatus = e -> {
      try {
        MyMasternodePanel.this.updateStatusLabels();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        MyMasternodePanel.this.errorReporter.reportError(ex);
      }
    };
    Timer syncTimer = new Timer(1000, alDeamonStatus);
    syncTimer.start();
    this.timers.add(syncTimer);
  }

  private void updateStatusLabels()
      throws IOException, InterruptedException {

    String text = "";
    try {
      text = this.clientCaller.getMasternodeSyncStatus(true);
    } catch (Exception e) {
      //TODO: handle exception
    }

    this.daemonStatusLabel.setText(text);
  }


  private void updateMasternodesTable()
      throws WalletCallException, IOException, InterruptedException {
    String[][] newMasternodesData = this.transactionGatheringThread.getLastData();

    // May be null - not even gathered once
    if (newMasternodesData == null) {
      return;
    }

    if (Util.arraysAreDifferent(lastMasternodesData, newMasternodesData)) {
      Log.info("Updating table of transactions");
      this.remove(transactionsTablePane);
      this.add(transactionsTablePane = new JScrollPane(
              transactionsTable = this.createMasternodesTable(newMasternodesData)),
          BorderLayout.CENTER);
    }

    lastMasternodesData = newMasternodesData;

    this.validate();
    this.repaint();
  }


  private JTable createMasternodesTable(String rowData[][])
      throws WalletCallException, IOException, InterruptedException {
    String columnNames[] = {LOCAL_MSG_MYMSTRNDE_ALIAS, LOCAL_MSG_MYMSTRNDE_ADDRESS, LOCAL_MSG_MYMSTRNDE_PRIVATEKEY, LOCAL_MSG_MYMSTRNDE_TXHASH, LOCAL_MSG_MYMSTRNDE_OUTPUTINDEX, LOCAL_MSG_MYMSTRNDE_STATUS};


    JTable table = new MasternodeTable(
        rowData, columnNames, this.parentFrame, this.clientCaller, this.installationObserver);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    table.getColumnModel().getColumn(0).setPreferredWidth(300);
    table.getColumnModel().getColumn(1).setPreferredWidth(110);
    table.getColumnModel().getColumn(2).setPreferredWidth(100);
    table.getColumnModel().getColumn(3).setPreferredWidth(300);
    table.getColumnModel().getColumn(4).setPreferredWidth(180);
    table.getColumnModel().getColumn(5).setPreferredWidth(100);

    return table;
  }


  private String[][] getMasternodeListFromRPC() throws WalletCallException, IOException, InterruptedException {

    String[][] myMasternodes = this.clientCaller.getMyMasternodes();
    return myMasternodes;
  }
}
