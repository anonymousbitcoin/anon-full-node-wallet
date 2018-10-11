package org.anonymous.wallets.fullnode.ui;


import org.anonymous.wallets.fullnode.daemon.ANONClientCaller;
import org.anonymous.wallets.fullnode.daemon.ANONClientCaller.ShieldCoinbaseResponse;
import org.anonymous.wallets.fullnode.util.Log;
import org.anonymous.wallets.fullnode.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Table to be used for addresses - specifically.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class AddressTable
    extends DataTable
{

  protected JPopupMenu zAddressPopupMenu;
  protected ANONClientCaller caller;
  private static final String LOCAL_MENU_GET_PK = Util.local("LOCAL_MENU_GET_PK");
  private static final String LOCAL_MENU_PK_INFO_1 = Util.local("LOCAL_MENU_PK_INFO_1");
  private static final String LOCAL_MENU_PK_INFO_2 = Util.local("LOCAL_MENU_PK_INFO_2");
  private static final String LOCAL_MENU_PK_INFO_3 = Util.local("LOCAL_MENU_PK_INFO_3");
  private static final String LOCAL_MSG_ERROR_GET_PK = Util.local("LOCAL_MSG_ERROR_GET_PK");

  public AddressTable(final Object[][] rowData, final Object[] columnNames,
                      final ANONClientCaller caller)
  {
    super(rowData, columnNames);

    this.caller = caller;

    // instantiate and do basic setup for popup menu for z-addresses
    zAddressPopupMenu = new JPopupMenu();
    zAddressPopupMenu.add(instantiateCopyMenuItem());
    zAddressPopupMenu.add(instantiateExportToCSVMenuItem());

    int accelaratorKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    popupMenu.add(instantiateObtainPrivateKeyMenuItem());
    // popupMenu.add(instantiateLockMenuItem());
    popupMenu.add(instantiateUnlockMenuItem());
    zAddressPopupMenu.add(instantiateObtainPrivateKeyMenuItem());

    zAddressPopupMenu.add(instantiateShieldAllCoinbaseMenuItem());


  } // End constructor

  protected JMenuItem instantiateLockMenuItem() {
		JMenuItem menuItem = new JMenuItem("Lock");
        //copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, accelaratorKeyMask));
        menuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if ((lastRow >= 0) && (lastColumn >= 0))
                {
                    String text = AddressTable.this.getValueAt(lastRow, lastColumn).toString();

                    try {
                      String message = caller.lockAddress(text);

                      JOptionPane.showMessageDialog(
                        AddressTable.this.getRootPane().getParent(), message,
                        "", JOptionPane.INFORMATION_MESSAGE);
                  } catch (Exception exception) {
                      //TODO: handle exception
                      Log.info("EXCEPTION: ");
                      Log.info(exception.toString());
                  }
                } else
                {
                    // Log perhaps
                }
            }
        });
        return menuItem;
  }
  protected JMenuItem instantiateUnlockMenuItem() {
		JMenuItem menuItem = new JMenuItem("Unlock");
        //copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, accelaratorKeyMask));
        menuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if ((lastRow >= 0) && (lastColumn >= 0))
                {
                    String text = AddressTable.this.getValueAt(lastRow, lastColumn).toString();

                    try {
                      String message = caller.unlockAddress(text);

                      JOptionPane.showMessageDialog(
                        AddressTable.this.getRootPane().getParent(), message,
                        "", JOptionPane.INFORMATION_MESSAGE);
                  } catch (Exception exception) {
                      //TODO: handle exception
                      Log.info("EXCEPTION: ");
                      Log.info(exception.toString());
                  }
                } else
                {
                    // Log perhaps
                }
            }
        });
        return menuItem;
	}

  protected JMenuItem instantiateObtainPrivateKeyMenuItem() {
    JMenuItem menuItem = new JMenuItem(LOCAL_MENU_GET_PK);
    //obtainPrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelaratorKeyMask));

    menuItem.addActionListener(e -> {
      if ((lastRow >= 0) && (lastColumn >= 0))
      {
        try
        {
          String address = AddressTable.this.getModel().getValueAt(lastRow, 2).toString();
          boolean isZAddress = Util.isZAddress(address);

          // Check for encrypted wallet
          final boolean bEncryptedWallet = caller.isWalletEncrypted();
          if (bEncryptedWallet)
          {
            PasswordDialog pd = new PasswordDialog((JFrame)(AddressTable.this.getRootPane().getParent()));
            pd.setVisible(true);

            if (!pd.isOKPressed())
            {
              return;
            }

            caller.unlockWallet(pd.getPassword());
          }

          String privateKey = isZAddress ?
              caller.getZPrivateKey(address) : caller.getTPrivateKey(address);

          // Lock the wallet again
          if (bEncryptedWallet)
          {
            caller.lockWallet();
          }

          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          clipboard.setContents(new StringSelection(privateKey), null);

          JOptionPane.showMessageDialog(
              AddressTable.this.getRootPane().getParent(),
              LOCAL_MENU_PK_INFO_1 + "\n" +
                  address + "\n" +
                  LOCAL_MENU_PK_INFO_2 + "\n" +
                  privateKey + "\n\n" +
                  LOCAL_MENU_PK_INFO_3,
              "Private Key", JOptionPane.INFORMATION_MESSAGE);


        } catch (Exception ex) {
          Log.error("Unexpected error: ", ex);
          JOptionPane.showMessageDialog(
              AddressTable.this.getRootPane().getParent(),
              LOCAL_MSG_ERROR_GET_PK + ": \n" +
                  ex.getMessage() + "\n\n",
              LOCAL_MSG_ERROR_GET_PK,
              JOptionPane.ERROR_MESSAGE);
        }
      } else
      {
        // Log perhaps
      }
    });
    return menuItem;
  }

  protected JMenuItem instantiateShieldAllCoinbaseMenuItem() {
    JMenuItem shieldAllCoinbaseFundsMenuItem = new JMenuItem("Shield all coinbases to this z-address");
    //obtainPrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelaratorKeyMask));

    shieldAllCoinbaseFundsMenuItem.addActionListener(e -> {
      if ((lastRow >= 0) && (lastColumn >= 0))
      {
        try
        {
          String address = AddressTable.this.getModel().getValueAt(lastRow, 2).toString();
          boolean isZAddress = Util.isZAddress(address);

          if (!isZAddress) {
            // TODO: this should never happen- gracefully error out
            return;
          }
          // Check for encrypted wallet
          final boolean bEncryptedWallet = caller.isWalletEncrypted();
          if (bEncryptedWallet)
          {
            PasswordDialog pd = new PasswordDialog((JFrame)(AddressTable.this.getRootPane().getParent()));
            pd.setVisible(true);

            if (!pd.isOKPressed())
            {
              return;
            }

            caller.unlockWallet(pd.getPassword());
          }

          ShieldCoinbaseResponse shieldCoinbaseResponse = caller.shieldCoinbase("*", address);

          // Lock the wallet again
          if (bEncryptedWallet)
          {
            caller.lockWallet();
          }

          JOptionPane.showMessageDialog(
              AddressTable.this.getRootPane().getParent(),
              "Coinbase funds in the amount of " + shieldCoinbaseResponse.shieldedValue + " ANON from " + shieldCoinbaseResponse.shieldedUTXOs + " UTXO" + (shieldCoinbaseResponse.shieldedUTXOs == 1 ? "" : "s") + " were shielded to the following z-address:\n" + address + "\nPlease Refresh to update balances.\n",
              "Shield All Coinbases", JOptionPane.INFORMATION_MESSAGE);

          // TODO: trigger refresh of window

        } catch (Exception ex)
        {
          Log.error("Unexpected error: ", ex);
          JOptionPane.showMessageDialog(
              AddressTable.this.getRootPane().getParent(),
              "Error:" + "\n" +
                  ex.getMessage() + "\n\n",
              "Error in shielding all coinbases!",
              JOptionPane.ERROR_MESSAGE);
        }
      } else
      {
        // Log perhaps
      }
    });
    return shieldAllCoinbaseFundsMenuItem;
  }
  @Override
  protected JPopupMenu getPopupMenu(int row, int column)
  {
    String address = AddressTable.this.getModel().getValueAt(row, 2).toString();
    boolean isZAddress = Util.isZAddress(address);
    if (isZAddress)
    {
      return zAddressPopupMenu;
    }
    return popupMenu;
  }

}