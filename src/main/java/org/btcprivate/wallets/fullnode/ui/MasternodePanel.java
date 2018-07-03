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
public class MasternodePanel
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
  private static final String LOCAL_MSG_MSTRNDE_ALIAS = Util.local("LOCAL_MSG_MSTRNDE_ALIAS");
  private static final String LOCAL_MSG_MSTRNDE_STATUS = Util.local("LOCAL_MSG_MSTRNDE_STATUS");
  private static final String LOCAL_MSG_MSTRNDE_PROTOCOL = Util.local("LOCAL_MSG_MSTRNDE_PROTOCOL");
  private static final String LOCAL_MSG_MSTRNDE_PAYEE = Util.local("LOCAL_MSG_MSTRNDE_PAYEE");
  private static final String LOCAL_MSG_MSTRNDE_LASTSEEN = Util.local("LOCAL_MSG_MSTRNDE_LASTSEEN");
  private static final String LOCAL_MSG_MSTRNDE_ACTIVETIME = Util.local("LOCAL_MSG_MSTRNDE_ACTIVETIME");
  private static final String LOCAL_MSG_MSTRNDE_LASTPAIDTIME = Util.local("LOCAL_MSG_MSTRNDE_LASTPAIDTIME");
  private static final String LOCAL_MSG_MSTRNDE_LASTBLOCK = Util.local("LOCAL_MSG_MSTRNDE_LASTBLOCK");
  private static final String LOCAL_MSG_MSTRNDE_IP = Util.local("LOCAL_MSG_MSTRNDE_IP");

  private static final String LOCAL_MSG_SYNC = Util.local("LOCAL_MSG_SYNC");
  private static final String LOCAL_MSG_BLOCK = Util.local("LOCAL_MSG_BLOCK");

  private static final String daemon_txn_receive = "receive";
  private static final String daemon_txn_send = "send";
  private static final String daemon_txn_mined = "generate";
  private static final String daemon_txn_unconfirmed = "immature";


  public MasternodePanel(JFrame parentFrame,
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

    lastMasternodesData = getMasternodeListFromRPC();
    dashboard.add(transactionsTablePane = new JScrollPane(
            transactionsTable = this.createMasternodesTable(lastMasternodesData)),BorderLayout.CENTER);

    dashboard.add(daemonStatusLabel = new JLabel(), BorderLayout.NORTH);

    ActionListener alDeamonStatus = e -> {
      try {
        MasternodePanel.this.updateStatusLabels();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        MasternodePanel.this.errorReporter.reportError(ex);
      }
    };
    Timer syncTimer = new Timer(1000, alDeamonStatus);
    syncTimer.start();
    this.timers.add(syncTimer);

    this.transactionGatheringThread = new DataGatheringThread<>(
        () -> {
          long start = System.currentTimeMillis();
          String[][] data = MasternodePanel.this.getMasternodeListFromRPC();
          long end = System.currentTimeMillis();
          Log.info("Refreshing Master node list " + (end - start) + "ms.");

          return data;
        },
        this.errorReporter, 20000);
    this.threads.add(this.transactionGatheringThread);

    ActionListener alMasternodes = e -> {
      try {
        MasternodePanel.this.updateMasternodesTable();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        MasternodePanel.this.errorReporter.reportError(ex);
      }
    };
    Timer t = new Timer(5000, alMasternodes);
    t.start();
    this.timers.add(t);

  }

  private void updateStatusLabels()
      throws IOException, InterruptedException {
  
    String text = "text";
    try {
      text = this.clientCaller.getMasternodeSyncStatus(false);
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
    String columnNames[] = {LOCAL_MSG_MSTRNDE_ALIAS, LOCAL_MSG_MSTRNDE_STATUS, LOCAL_MSG_MSTRNDE_PROTOCOL, LOCAL_MSG_MSTRNDE_PAYEE, LOCAL_MSG_MSTRNDE_LASTSEEN, LOCAL_MSG_MSTRNDE_ACTIVETIME, LOCAL_MSG_MSTRNDE_LASTPAIDTIME, LOCAL_MSG_MSTRNDE_LASTBLOCK, LOCAL_MSG_MSTRNDE_IP};
    JTable table = new MasternodeTable(
        rowData, columnNames, this.parentFrame, this.clientCaller, this.installationObserver);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    table.getColumnModel().getColumn(0).setPreferredWidth(300);
    table.getColumnModel().getColumn(1).setPreferredWidth(110);
    table.getColumnModel().getColumn(2).setPreferredWidth(100);
    table.getColumnModel().getColumn(3).setPreferredWidth(300);
    table.getColumnModel().getColumn(4).setPreferredWidth(180);
    table.getColumnModel().getColumn(5).setPreferredWidth(100);
    table.getColumnModel().getColumn(6).setPreferredWidth(50);
    table.getColumnModel().getColumn(7).setPreferredWidth(50);
    table.getColumnModel().getColumn(8).setPreferredWidth(250);

    return table;
  }

  

  
  private String[][] getMasternodeListFromRPC()
  throws WalletCallException, IOException, InterruptedException {
    String[][] mnListArrays = this.clientCaller.getMasternodeList();
    return mnListArrays;

  }
} // End class