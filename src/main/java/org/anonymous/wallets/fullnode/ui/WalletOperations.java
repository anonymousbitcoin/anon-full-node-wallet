package org.anonymous.wallets.fullnode.ui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


import org.anonymous.wallets.fullnode.daemon.ANONClientCaller;
import org.anonymous.wallets.fullnode.daemon.ANONClientCaller.*;
import org.anonymous.wallets.fullnode.daemon.ANONInstallationObserver;
import org.anonymous.wallets.fullnode.util.BackupTracker;
import org.anonymous.wallets.fullnode.util.Log;
import org.anonymous.wallets.fullnode.util.StatusUpdateErrorReporter;
import org.anonymous.wallets.fullnode.util.Util;


/**
 * Provides miscellaneous operations for the wallet file.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class WalletOperations {
  private ANONWalletUI parent;
  private JTabbedPane tabs;
  private AddressesPanel addresses;

  private ANONClientCaller clientCaller;
  private StatusUpdateErrorReporter errorReporter;

  private static final String LOCAL_MENU_PK_INFO_1 = Util.local("LOCAL_MENU_PK_INFO_1");
  private static final String LOCAL_MENU_PK_INFO_2 = Util.local("LOCAL_MENU_PK_INFO_2");
  private static final String LOCAL_MENU_PK_INFO_3 = Util.local("LOCAL_MENU_PK_INFO_3");
  private static final String LOCAL_MENU_SELECT_TO_VIEW_PK = Util.local("LOCAL_MENU_SELECT_TO_VIEW_PK");
  private static final String LOCAL_MEN_SELECT_ADDRESS = Util.local("LOCAL_MEN_SELECT_ADDRESS");
  private static final String LOCAL_MENU_PK = Util.local("LOCAL_MENU_PK");


  public WalletOperations(ANONWalletUI parent,
                          JTabbedPane tabs,
                          AddressesPanel addresses,
                          ANONClientCaller clientCaller,
                          StatusUpdateErrorReporter errorReporter)
      throws IOException, InterruptedException, WalletCallException {
    this.parent = parent;
    this.tabs = tabs;
    this.addresses = addresses;

    this.clientCaller = clientCaller;
    this.errorReporter = errorReporter;

  }

  public void showPrivateKey() {
    if (this.tabs.getSelectedIndex() != 1) {
      JOptionPane.showMessageDialog(
          this.parent, LOCAL_MENU_SELECT_TO_VIEW_PK,
          LOCAL_MEN_SELECT_ADDRESS, JOptionPane.INFORMATION_MESSAGE);
      this.tabs.setSelectedIndex(1);
      return;
    }

    String address = this.addresses.getSelectedAddress();

    if (address == null) {
      JOptionPane.showMessageDialog(
          this.parent,
          LOCAL_MENU_SELECT_TO_VIEW_PK,
          LOCAL_MEN_SELECT_ADDRESS, JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    try {
      // Check for encrypted wallet
      final boolean bEncryptedWallet = this.clientCaller.isWalletEncrypted();
      if (bEncryptedWallet) {
        PasswordDialog pd = new PasswordDialog((this.parent));
        pd.setVisible(true);

        if (!pd.isOKPressed()) {
          return;
        }

        this.clientCaller.unlockWallet(pd.getPassword());
      }

      boolean isZAddress = Util.isZAddress(address);

      String privateKey = isZAddress ?
          this.clientCaller.getZPrivateKey(address) : this.clientCaller.getTPrivateKey(address);

      // Lock the wallet again
      if (bEncryptedWallet) {
        this.clientCaller.lockWallet();
      }

      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(new StringSelection(privateKey), null);

      JOptionPane.showMessageDialog(
          this.parent,
          LOCAL_MENU_PK_INFO_1 + "\n" +
              address + "\n" +
              LOCAL_MENU_PK_INFO_2 + "\n" +
              privateKey + "\n\n" +
              LOCAL_MENU_PK_INFO_3,
          LOCAL_MENU_PK, JOptionPane.INFORMATION_MESSAGE);


    } catch (Exception ex) {
      this.errorReporter.reportError(ex, false);
    }
  }


  public void importSinglePrivateKey() {
    try {
      SingleKeyImportDialog kd = new SingleKeyImportDialog(this.parent, this.clientCaller);
      kd.setVisible(true);

    } catch (Exception ex) {
      this.errorReporter.reportError(ex, false);
    }
  }
}