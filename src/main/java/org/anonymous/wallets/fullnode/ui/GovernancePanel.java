package org.anonymous.wallets.fullnode.ui;

import org.anonymous.wallets.fullnode.daemon.ANONClientCaller;
import org.anonymous.wallets.fullnode.daemon.ANONClientCaller.NetworkAndBlockchainInfo;
import org.anonymous.wallets.fullnode.daemon.ANONClientCaller.WalletBalance;
import org.anonymous.wallets.fullnode.daemon.ANONClientCaller.WalletCallException;
import org.anonymous.wallets.fullnode.daemon.ANONInstallationObserver;
import org.anonymous.wallets.fullnode.daemon.ANONInstallationObserver.DaemonInfo;
import org.anonymous.wallets.fullnode.daemon.DataGatheringThread;
import org.anonymous.wallets.fullnode.util.*;
import org.anonymous.wallets.fullnode.util.OSUtil.OS_TYPE;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import com.google.common.reflect.TypeResolver;

import java.awt.*;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;


/**
 * Dashboard ...
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
@SuppressWarnings({"deprecation"})
public class GovernancePanel
    extends WalletTabPanel {
  private JFrame parentFrame;
  private ANONInstallationObserver installationObserver;
  private ANONClientCaller clientCaller;
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
  private String[][] lastGovernancesData = null;
  private DataGatheringThread<String[][]> transactionGatheringThread = null;

  private JComboBox voteOutcome = null;
  private JComboBox voteSignal = null;
  private JComboBox myMasternodeAliasList = null;
  private JPanel comboBoxParentPanel = null;
  private WalletTextField gobjectTxHash = null;
  final JDialog frame = new JDialog(parentFrame, "Enter the Prepare Command", true);
  
    
  JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

  private static final String small_icon_resource = "images/anon-44.png";

  private static final String LOCAL_MSG_ANON_WALLET_TITLE = Util.local("LOCAL_MSG_ANON_WALLET_TITLE");
  private static final String LOCAL_MSG_ANON_WALLET_TOOLTIP = Util.local("LOCAL_MSG_ANON_WALLET_TOOLTIP");
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

  private static final String LOCAL_MSG_GOVERNANCE_STARTEPOCH = Util.local("LOCAL_MSG_GOVERNANCE_STARTEPOCH");
  private static final String LOCAL_MSG_GOVERNANCE_NAME = Util.local("LOCAL_MSG_GOVERNANCE_NAME");
  private static final String LOCAL_MSG_GOVERNANCE_HASH = Util.local("LOCAL_MSG_GOVERNANCE_HASH");
  private static final String LOCAL_MSG_GOVERNANCE_PAYMENTADDRESS = Util.local("LOCAL_MSG_GOVERNANCE_PAYMENTADDRESS");
  private static final String LOCAL_MSG_GOVERNANCE_PAYMENTAMOUNT = Util.local("LOCAL_MSG_GOVERNANCE_PAYMENTAMOUNT");
  private static final String LOCAL_MSG_GOVERNANCE_ENDEPOCH = Util.local
  ("LOCAL_MSG_GOVERNANCE_ENDEPOCH");
  private static final String LOCAL_MSG_GOVERNANCE_TYPE = Util.local
  ("LOCAL_MSG_GOVERNANCE_TYPE");
  
  

  private static final String LOCAL_MSG_SYNC = Util.local("LOCAL_MSG_SYNC");
  private static final String LOCAL_MSG_BLOCK = Util.local("LOCAL_MSG_BLOCK");

  private static final String daemon_txn_receive = "receive";
  private static final String daemon_txn_send = "send";
  private static final String daemon_txn_mined = "generate";
  private static final String daemon_txn_unconfirmed = "immature";


  public GovernancePanel(JFrame parentFrame,
                        ANONInstallationObserver installationObserver,
                        ANONClientCaller clientCaller,
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
    
    gobjectTxHash = new WalletTextField(13);
    
    voteOutcome = new JComboBox<>(new String[]{"Funding","Delete","Valid"});
    voteSignal = new JComboBox<>(new String[]{"Yes", "No", "Abstain"});
    String[] myMasternodeAliases = this.clientCaller.getMyMasternodesAliases().length != 0 ? this.clientCaller.getMyMasternodesAliases() : new String[]{"No Masternodes Available"};
    myMasternodeAliasList = new JComboBox<>(myMasternodeAliases);
    comboBoxParentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    comboBoxParentPanel.add(new JLabel("Proposal TX Hash"));
    comboBoxParentPanel.add(gobjectTxHash);
    comboBoxParentPanel.add(voteOutcome);
    comboBoxParentPanel.add(voteSignal);
    comboBoxParentPanel.add(myMasternodeAliasList);
    buttonPanel.add(comboBoxParentPanel);

    
    // JButton startAliasButton = new JButton(LOCAL_MENU_NEW_B_ADDRESS);
    JButton startAliasButton = new JButton("Vote Alias");
    buttonPanel.add(startAliasButton);

    // JButton startAllButton = new JButton(LOCAL_MENU_NEW_Z_ADDRESS);
    JButton startAllButton = new JButton("Vote All");
    buttonPanel.add(startAllButton);

    JButton createNewGovobjectButton = new JButton("Create New Proposal");
    buttonPanel.add(createNewGovobjectButton);

    // JButton updateTableButton = new JButton(LOCAL_MENU_REFRESH);
    JButton updateTableButton = new JButton("Update Table");
    // buttonPanel.add(updateTableButton);

    JLabel updateLabelStart = new JLabel("Table is updated every 5 seconds");
    // buttonPanel.add(updateLabelStart);

    dashboard.add(buttonPanel, BorderLayout.SOUTH);

    startAliasButton.addActionListener(e -> {
      try {
        this.voteAlias();
      } catch (Exception exception) {
        Log.info(exception.toString());
      }
    });

    ActionListener updateTimer = e -> {
      try
      {
        if (counter == 0){
          // startAllButton.setText("Start All");
          // startAliasButton.setText("Start Alias");
          // startMissingButton.setText("Start Missing");
          // updateTableButton.setText("Update Table");
          // resetMnsyncButton.setText("Reset Sync");
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

    createNewGovobjectButton.addActionListener(e -> {
      try{
        this.createProposalSubmissionModal();
      } catch (Exception ex)
      {
        Log.error("Eror in createNewGovobjectButton: " + ex);
      }
      counter = 5;
    });

    startAllButton.addActionListener(e -> {
      try{
        this.voteAll();
      }catch (Exception ex){
        Log.error("Error in startAllButton: " + ex);
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

    lastGovernancesData = getGovernanceListFromRPC();
    dashboard.add(daemonStatusLabel = new JLabel(), BorderLayout.NORTH);
    
    dashboard.add(transactionsTablePane = new JScrollPane(
            transactionsTable = this.createGovernancesTable(lastGovernancesData)),BorderLayout.CENTER);


    // Thread and timer to update the transactions table
    this.transactionGatheringThread = new DataGatheringThread<>(
        () -> {
          long start = System.currentTimeMillis();
          String[][] data = GovernancePanel.this.getGovernanceListFromRPC();
          long end = System.currentTimeMillis();
          Log.info("Gathering MY MASTERNODES: " + (end - start) + "ms.");

          return data;
        },
        this.errorReporter, 20000);
    this.threads.add(this.transactionGatheringThread);

    ActionListener alGovernances = e -> {
      try {
        GovernancePanel.this.updateGovernancesTable();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        GovernancePanel.this.errorReporter.reportError(ex);
      }
    };
    Timer t = new Timer(5000, alGovernances);
    t.start();
    this.timers.add(t);

    ActionListener alDeamonStatus = e -> {
      
    };
    Timer syncTimer = new Timer(1000, alDeamonStatus);
    syncTimer.start();
    this.timers.add(syncTimer);

    WalletTextField cliPrepareField = new WalletTextField(21);
    WalletTextField cliSubmitField = new WalletTextField(21);
    frame.setPreferredSize(new Dimension(500, 250));
    
    panel.setLayout(new FlowLayout(FlowLayout.LEADING, 3, 3));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(cliPrepareField);
    JButton prepareButton = new JButton("Prepare Proposal");
    panel.add(prepareButton);
    panel.add(cliSubmitField);

    JLabel submissionInstruction = new JLabel("Paste the submission command and the Transaction ID after it\n");
    JButton submitButton = new JButton("Submit Proposal");
    panel.add(submitButton);
    JTextArea responseMessage = new JTextArea();
    panel.add(responseMessage);
    panel.add(submissionInstruction);

    responseMessage.setEditable(false); // as before
    responseMessage.setBackground(null); // this is the same as a JLabel
    responseMessage.setBorder(null); 

    prepareButton.addActionListener(e -> {
      try{
        String response = this.clientCaller.prepareCommand(cliPrepareField.getText());
        responseMessage.setText("Transaction ID: \n" + response);
      } catch (Exception ex){
        Log.error("Error in prepareButton: " + ex);
        responseMessage.setText("Error: \n" + ex);
      }
    });

    submitButton.addActionListener(e -> {
      try{
        String response = this.clientCaller.submitCommand(cliSubmitField.getText());
        responseMessage.setText("Submitted: \n" + response);
      } catch (Exception ex){
        Log.error("Error in prepareButton: " + ex);
        responseMessage.setText("Error: \n" + ex);
      }
    });

  }

  private void createProposalSubmissionModal() {

    frame.getContentPane().add(panel);
    frame.pack();
    frame.setVisible(true);

    
  }

  private void voteAll() 
      throws WalletCallException, IOException, InterruptedException {
    String voteOutcome = this.voteOutcome.getItemAt(this.voteOutcome.getSelectedIndex()).toString();
    String voteSignal = this.voteSignal.getItemAt(this.voteSignal.getSelectedIndex()).toString();
    String gobjectTxHash = this.gobjectTxHash.getText();
    String[] overallResult = this.clientCaller.gobjectVoteAll(voteOutcome, voteSignal, gobjectTxHash);

    Object[] options = {"Ok"};

    int option = JOptionPane.showOptionDialog(
        GovernancePanel.this.getRootPane().getParent(),
        "\nResult:" + " " + overallResult[0] + "\n" +
            overallResult[1] + "\n" + "" + "\n" ,
        "Gobject Voting Result",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        null,
        options,
        options[0]);
  }

  private void voteAlias() throws WalletCallException, IOException, InterruptedException {
      String voteOutcome = this.voteOutcome.getItemAt(this.voteOutcome.getSelectedIndex()).toString();
      String voteSignal = this.voteSignal.getItemAt(this.voteSignal.getSelectedIndex()).toString();
      String gobjectTxHash = this.gobjectTxHash.getText();
      String masternodeAlias = this.myMasternodeAliasList.getItemAt(this.myMasternodeAliasList.getSelectedIndex()).toString();
      String[] overallResult = this.clientCaller.gobjectVoteAliases(voteOutcome, voteSignal, gobjectTxHash, masternodeAlias);

      Object[] options = {"Ok"};

      int option = JOptionPane.showOptionDialog(
        GovernancePanel.this.getRootPane().getParent(),
        "\nResult:" + " " + overallResult[0] + "\n" +
            overallResult[1] + "\n" + "" + "\n" ,
        "Gobject Voting Result",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        null,
        options,
        options[0]);
  }

  private void updateGovernancesTable()
      throws WalletCallException, IOException, InterruptedException {
    String[][] newGovernancesData = this.transactionGatheringThread.getLastData();

    // May be null - not even gathered once
    if (newGovernancesData == null) {
      return;
    }

    if (Util.arraysAreDifferent(lastGovernancesData, newGovernancesData)) {
      Log.info("Updating table of transactions");
      this.remove(transactionsTablePane);
      this.add(transactionsTablePane = new JScrollPane(
              transactionsTable = this.createGovernancesTable(newGovernancesData)),
          BorderLayout.CENTER);
    }

    lastGovernancesData = newGovernancesData;

    this.validate();
    this.repaint();
  }


  private JTable createGovernancesTable(String rowData[][])
      throws WalletCallException, IOException, InterruptedException {
    String columnNames[] = {LOCAL_MSG_GOVERNANCE_HASH, LOCAL_MSG_GOVERNANCE_STARTEPOCH, LOCAL_MSG_GOVERNANCE_NAME, LOCAL_MSG_GOVERNANCE_PAYMENTADDRESS, LOCAL_MSG_GOVERNANCE_PAYMENTAMOUNT, LOCAL_MSG_GOVERNANCE_ENDEPOCH, LOCAL_MSG_GOVERNANCE_TYPE};

    JTable table = new MasternodeTable(
          rowData, columnNames, this.parentFrame, this.clientCaller, this.installationObserver);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
      table.getColumnModel().getColumn(0).setPreferredWidth(300);
      table.getColumnModel().getColumn(1).setPreferredWidth(110);
      table.getColumnModel().getColumn(2).setPreferredWidth(100);
      table.getColumnModel().getColumn(3).setPreferredWidth(200);
      table.getColumnModel().getColumn(4).setPreferredWidth(180);
      table.getColumnModel().getColumn(5).setPreferredWidth(100);

      return table;
    
  }


  private String[][] getGovernanceListFromRPC() throws WalletCallException, IOException, InterruptedException {

    String[][] myGovernances = this.clientCaller.getGobjectList();
    return myGovernances;
  }
}
