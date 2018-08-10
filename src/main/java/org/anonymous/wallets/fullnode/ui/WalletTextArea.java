package org.anonymous.wallets.fullnode.ui;

import javax.swing.*;
import org.anonymous.wallets.fullnode.util.Util;
	
public class WalletTextArea
        extends JTextArea
{
    public WalletTextArea(int rows, int columns)
    {
        super(rows, columns);
    }
        
    public String getText()
    {
    	return Util.removeUTF8BOM(super.getText());
    }

} // End class