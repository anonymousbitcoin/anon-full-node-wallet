package org.anonymous.wallets.fullnode.daemon;


import com.eclipsesource.json.*;
import org.anonymous.wallets.fullnode.util.Log;
import org.anonymous.wallets.fullnode.util.OSUtil;
import org.anonymous.wallets.fullnode.util.OSUtil.OS_TYPE;
import org.anonymous.wallets.fullnode.util.Util;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class ANONClientCaller {

    public static class WalletBalance {
        public double transparentBalance;
        public double privateBalance;
        public double totalBalance;
        public double masternodeCollateral;

        public double transparentUnconfirmedBalance;
        public double privateUnconfirmedBalance;
        public double totalUnconfirmedBalance;
        public double unconfirmedMasternodeCollateral;
    }

    public static class Masternode {

        public String mnStatus;
        public String mnProtocol;
        public String mnPayee;
        public String mnLastSeen;
        public String mnActiveSeconds;
        public String mnLastPaidTime;
        public String mnLastPaidBlock;
        public String mnIP;

    }

    public static class ShieldCoinbaseResponse {
        public String operationid;
        public int shieldedUTXOs;
        public double shieldedValue;
        public int remainingUTXOs;
        public double remainingValue;
    }


    public static class NetworkAndBlockchainInfo {
        public int numConnections;
        public Date lastBlockDate;
        public String lastBlockHeight;
    }


    public static class WalletCallException
            extends Exception {
        public WalletCallException(String message) {
            super(message);
        }

        public WalletCallException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    // ZCash client program and daemon
    private File zcashcli, zcashd;


    public ANONClientCaller(String installDir)
            throws IOException {
        // Detect daemon and client tools installation
        File dir = new File(installDir);
        zcashcli = new File(dir, OSUtil.getZCashCli());

        if (!zcashcli.exists()) {
            zcashcli = OSUtil.findZCashCommand(OSUtil.getZCashCli());
        }

        if ((zcashcli == null) || (!zcashcli.exists())) {
            throw new IOException(
                    "TheAnonymousDesktop Wallet installation directory (" + installDir + ") needs to contain " +
                            "the command line utilities anond and anon-cli. anon-cli is missing!");
        }

        zcashd = new File(dir, OSUtil.getZCashd());
        if (!zcashd.exists()) {
            zcashd = OSUtil.findZCashCommand(OSUtil.getZCashd());
        }

        if (zcashd == null || (!zcashd.exists())) {
            throw new IOException(
                    "TheAnonymouscommand line    utility " + zcashcli.getCanonicalPath() +
                            " was found, but anond was not found!");
        }
    }


    public synchronized Process startDaemon()
            throws IOException, InterruptedException {
        String exportDir = OSUtil.getUserHomeDirectory().getCanonicalPath();

        CommandExecutor starter = new CommandExecutor(
                new String[]
                        {
                                zcashd.getCanonicalPath(),
                                "-exportdir=" + exportDir
                        });

        return starter.startChildProcess();
    }


    public /*synchronized*/ void stopDaemon()
            throws IOException, InterruptedException {
        CommandExecutor stopper = new CommandExecutor(
                new String[]{zcashcli.getCanonicalPath(), "stop"});

        String result = stopper.execute();
        Log.info("Stop command issued: " + result);
    }


    public synchronized JsonObject getDaemonRawRuntimeInfo()
            throws IOException, InterruptedException, WalletCallException {
        CommandExecutor infoGetter = new CommandExecutor(
                new String[]{zcashcli.getCanonicalPath(), "getinfo"});
        String info = infoGetter.execute();
        Log.info(info.trim().toLowerCase(Locale.ROOT));

        if (info.trim().toLowerCase(Locale.ROOT).startsWith("error: couldn't connect to server")) {
            throw new IOException(info.trim());
        }

        if (info.trim().toLowerCase(Locale.ROOT).startsWith("error: ")) {
            info = info.substring(7);
            System.out.println(info);

            try {
                return Json.parse(info).asObject();
            } catch (ParseException pe) {
                Log.error("unexpected daemon info: " + info);
                throw new IOException(pe);
            }
        } else if (info.trim().toLowerCase(Locale.ROOT).startsWith("error code:")) {
            return Util.getJsonErrorMessage(info);
        } else {
            try {
                return Json.parse(info).asObject();
            } catch (ParseException pe) {
                Log.info("unexpected daemon info: " + info);
                throw new IOException(pe);
            }
        }
    }


    public synchronized WalletBalance getWalletInfo()
            throws WalletCallException, IOException, InterruptedException {
        WalletBalance balance = new WalletBalance();

        JsonObject objResponse = this.executeCommandAndGetJsonObject("z_gettotalbalance", null);

        balance.transparentBalance = Double.valueOf(objResponse.getString("transparent", "-1"));
        balance.privateBalance = Double.valueOf(objResponse.getString("private", "-1"));
        balance.totalBalance = Double.valueOf(objResponse.getString("total", "-1"));
        // balance.masternodeCollateral = Double.valueOf(objResponse.getString("masternode collaterals", "-1"));

        objResponse = this.executeCommandAndGetJsonObject("z_gettotalbalance", "0");

        balance.transparentUnconfirmedBalance = Double.valueOf(objResponse.getString("transparent", "-1"));
        balance.privateUnconfirmedBalance = Double.valueOf(objResponse.getString("private", "-1"));
        balance.totalUnconfirmedBalance = Double.valueOf(objResponse.getString("total", "-1"));
        // balance.unconfirmedMasternodeCollateral = Double.valueOf(objResponse.getString("masternode collaterals", "-1"));

        return balance;
    }

    public synchronized String[][] getMasternodeList() throws WalletCallException, IOException, InterruptedException {

        JsonArray objResponse = this.executeCommandAndGetJsonArray("masternodelist", "walletarray");

        String[][] finalArr = new String [objResponse.size()][];
        for(int i = 0 ; i < objResponse.size() ; i ++){
            finalArr[i] = new String[9];
            JsonArray trans = objResponse.get(i).asArray();

            finalArr[i][0] = trans.get(0).toString().replace("\"","");
            finalArr[i][1] = trans.get(1).toString().replace("\"","");
            finalArr[i][2] = trans.get(2).toString().replace("\"","");
            finalArr[i][3] = trans.get(3).toString().replace("\"","");
            finalArr[i][4] = trans.get(4).toString().replace("\"","");
            finalArr[i][5] = trans.get(5).toString().replace("\"","");
            finalArr[i][6] = trans.get(6).toString().replace("\"","");
            finalArr[i][7] = trans.get(7).toString().replace("\"","");
            finalArr[i][8] = trans.get(8).toString().replace("\"","");
            // String[] ar = finalArr[i];
            // for(int j = 0; j < ar.length; j++) {
            //     Log.info("----------------------------------\n");
            //     Log.info(finalArr[i][j]);
            //     // if(finalArr[i][j] < 0) {
            //         // Log.info("It is null");
            //     // }
            // }

        }
        // Log.info("----------------------------------\n");
        // Log.info(finalArr[1][1].toString());
        // Log.info("----------------------------------");
        return finalArr;
    }

    public synchronized String[][] getMyMasternodes() throws WalletCallException, IOException, InterruptedException {

        JsonArray objResponse = this.executeCommandAndGetJsonArray("masternode", "mymasternodes");

        if (objResponse.size() == 0){
            String[][] noMNArr = new String[1][];
            noMNArr[0] = new String[6];
            noMNArr[0][0] = "No Masternodes set in conf file.";
            noMNArr[0][1] = "";
            noMNArr[0][2] = "";
            noMNArr[0][3] = "";
            noMNArr[0][4] = "";
            noMNArr[0][5] = "";

            return noMNArr;
        };

        String[][] finalArr = new String [objResponse.size()][];
        for(int i = 0 ; i < objResponse.size() ; i ++){
            finalArr[i] = new String[6];
            JsonArray trans = objResponse.get(i).asArray();

            finalArr[i][0] = trans.get(0).toString().replace("\"","");
            finalArr[i][1] = trans.get(1).toString().replace("\"","");
            finalArr[i][2] = trans.get(2).toString().replace("\"","");
            finalArr[i][3] = trans.get(3).toString().replace("\"","");
            finalArr[i][4] = trans.get(4).toString().replace("\"","");
            finalArr[i][5] = trans.get(5).toString().replace("\"","");
        }

        return finalArr;
    }


    public synchronized String getMasternodeSyncStatus() throws WalletCallException, IOException, InterruptedException {

        JsonObject objResponse = this.executeCommandAndGetJsonObject("mnsync", "status");

        String assetID = objResponse.get("AssetID").toString();
        String assetName = objResponse.get("AssetName").toString();
        String attempt = objResponse.get("Attempt").toString();
        String isBlockchainSynced = objResponse.get("IsBlockchainSynced").toString();
        String isMasternodeListSynced = objResponse.get("IsMasternodeListSynced").toString();
        String isWinnersListSynced = objResponse.get("IsWinnersListSynced").toString();
        String isSynced = objResponse.get("IsSynced").toString();
        String isFailed = objResponse.get("IsFailed").toString();

        StringBuilder stringBuilder = new StringBuilder();
        String color, mnStatus;
        if (assetName.replace("\"", "").equals("MASTERNODE_SYNC_FINISHED")) {
            mnStatus = "Finished";
        } else if (assetName.equals("MASTERNODE_SYNC_FAILED")) {
            mnStatus = "Failed";
        } else {
            mnStatus = "Syncing";
        }
        color = assetName.equals( "MASTERNODE_SYNC_FAILED") ?  "red" : "green";
        // if(myMasternodeList) {
            stringBuilder.append("<html>");  
            stringBuilder.append("<span style=\"font-weight:bold;\">Masternode List Sync: </span>");
            stringBuilder.append("<span style='color:" + color + "'>" + mnStatus + "</span><br/>");
            stringBuilder.append("<html/>");
            
            String returnString = stringBuilder.toString().replace("\"","");

            return returnString;
        // }

        // stringBuilder.append("<html>");
        // stringBuilder.append("<span style=\"font-weight:bold;\">Asset ID: </span>");
        // stringBuilder.append("<span>" + assetID + "</span><br/>");
        // stringBuilder.append("<span style=\"font-weight:bold;\">Asset Name: </span>");
        // stringBuilder.append("<span>" + assetName + "</span><br/>");
        // stringBuilder.append("<span style=\"font-weight:bold;\">Attempt: </span>");
        // stringBuilder.append("<span>" + attempt + "</span><br/>");
        // stringBuilder.append("<span style=\"font-weight:bold;\">Is BlockChain Synced: </span>");
        // stringBuilder.append("<span>" + isBlockchainSynced + "</span><br/>");
        // stringBuilder.append("<span style=\"font-weight:bold;\">Is Masternode List Synced: </span>");
        // stringBuilder.append("<span>" + isMasternodeListSynced + "</span><br/>");
        // stringBuilder.append("<span style=\"font-weight:bold;\">Is Winners List Synced: </span>");
        // stringBuilder.append("<span>" + isWinnersListSynced + "</span><br/>");
        // stringBuilder.append("<span style=\"font-weight:bold;\">Is Synced: </span>");
        // stringBuilder.append("<span>" + isSynced + "</span><br/>");
        // stringBuilder.append("<span style=\"font-weight:bold;\">Is Failed: </span>");
        // stringBuilder.append("<span>" + isFailed + "</span><br/>");
        // stringBuilder.append("<html/>");

        // String returnString = stringBuilder.toString().replace("\"","");

        // return returnString;
    }

    public synchronized String[] getMyAliases() throws WalletCallException, IOException, InterruptedException {

        JsonArray objResponse = this.executeCommandAndGetJsonArray("masternode", "mymasternodes");

        String[] finalArr = new String [objResponse.size()];
        for(int i = 0 ; i < objResponse.size() ; i ++){
            JsonArray trans = objResponse.get(i).asArray();
            // trans.replace(" ", "")
            finalArr[i] = trans.get(0).toString().replace("\"", "");
        }

        return finalArr;
    }

    public synchronized String[][] getWalletPublicTransactions()
            throws WalletCallException, IOException, InterruptedException {
        String notListed = "\u26D4";

        OS_TYPE os = OSUtil.getOSType();
        if (os == OS_TYPE.WINDOWS) {
            notListed = " \u25B6";
        }

        JsonArray jsonTransactions = executeCommandAndGetJsonArray(
                "listtransactions", wrapStringParameter(""), "300");
        String strTransactions[][] = new String[jsonTransactions.size()][];
        for (int i = 0; i < jsonTransactions.size(); i++) {
            strTransactions[i] = new String[7];
            JsonObject trans = jsonTransactions.get(i).asObject();

            // Needs to be the same as in getWalletZReceivedTransactions()
            // TODO: some day refactor to use object containers
            strTransactions[i][0] = "\u2606T (Public)";
            strTransactions[i][1] = trans.getString("category", "ERROR!");
            strTransactions[i][2] = trans.get("confirmations").toString();
            strTransactions[i][3] = trans.get("amount").toString();
            strTransactions[i][4] = trans.get("time").toString();
            strTransactions[i][5] = trans.getString("address", notListed + " (Z Address not listed by wallet!)");
            strTransactions[i][6] = trans.get("txid").toString();

        }

        return strTransactions;
    }


    public synchronized String[] getWalletZAddresses()
            throws WalletCallException, IOException, InterruptedException {
        JsonArray jsonAddresses = executeCommandAndGetJsonArray("z_listaddresses", null);
        String strAddresses[] = new String[jsonAddresses.size()];
        for (int i = 0; i < jsonAddresses.size(); i++) {
            strAddresses[i] = jsonAddresses.get(i).asString();
        }

        return strAddresses;
    }



    public synchronized String[][] getWalletZReceivedTransactions()
            throws WalletCallException, IOException, InterruptedException {
        String[] zAddresses = this.getWalletZAddresses();

        List<String[]> zReceivedTransactions = new ArrayList<String[]>();

        for (String zAddress : zAddresses) {
            JsonArray jsonTransactions = executeCommandAndGetJsonArray(
                    "z_listreceivedbyaddress", wrapStringParameter(zAddress), "0");
            for (int i = 0; i < jsonTransactions.size(); i++) {
                String[] currentTransaction = new String[7];
                JsonObject trans = jsonTransactions.get(i).asObject();

                String txID = trans.getString("txid", "ERROR!");
                // Needs to be the same as in getWalletPublicTransactions()
                // TODO: some day refactor to use object containers
                currentTransaction[0] = "\u2605Z (Private)";
                currentTransaction[1] = "receive";
                currentTransaction[2] = this.getWalletTransactionConfirmations(txID);
                currentTransaction[3] = trans.get("amount").toString();
                currentTransaction[4] = this.getWalletTransactionTime(txID); // TODO: minimize sub-calls
                currentTransaction[5] = zAddress;
                currentTransaction[6] = trans.get("txid").toString();

                zReceivedTransactions.add(currentTransaction);
            }
        }

        return zReceivedTransactions.toArray(new String[0][]);
    }


    public synchronized JsonObject[] getTransactionMessagingDataForZaddress(String ZAddress)
            throws WalletCallException, IOException, InterruptedException {
        JsonArray jsonTransactions = executeCommandAndGetJsonArray(
                "z_listreceivedbyaddress", wrapStringParameter(ZAddress), "0");
        List<JsonObject> transactions = new ArrayList<JsonObject>();
        for (int i = 0; i < jsonTransactions.size(); i++) {
            JsonObject trans = jsonTransactions.get(i).asObject();
            transactions.add(trans);
        }

        return transactions.toArray(new JsonObject[0]);
    }


    // ./src/zcash-cli listunspent only returns T addresses it seems
    public synchronized String[] getWalletPublicAddressesWithUnspentOutputs()
            throws WalletCallException, IOException, InterruptedException {
        JsonArray jsonUnspentOutputs = executeCommandAndGetJsonArray("listunspent", "0");

        Set<String> addresses = new HashSet<>();
        for (int i = 0; i < jsonUnspentOutputs.size(); i++) {
            JsonObject outp = jsonUnspentOutputs.get(i).asObject();
            addresses.add(outp.getString("address", "ERROR!"));
        }

        return addresses.toArray(new String[0]);
    }


    // ./zcash-cli listreceivedbyaddress 0 true
    public synchronized String[] getWalletAllPublicAddresses()
            throws WalletCallException, IOException, InterruptedException {
        JsonArray jsonReceivedOutputs = executeCommandAndGetJsonArray("listreceivedbyaddress", "0", "true");

        Set<String> addresses = new HashSet<>();
        for (int i = 0; i < jsonReceivedOutputs.size(); i++) {
            JsonObject outp = jsonReceivedOutputs.get(i).asObject();
            addresses.add(outp.getString("address", "ERROR!"));
        }

        return addresses.toArray(new String[0]);
    }


    public synchronized Map<String, String> getRawTransactionDetails(String txID)
            throws WalletCallException, IOException, InterruptedException {
        JsonObject jsonTransaction = this.executeCommandAndGetJsonObject(
                "gettransaction", wrapStringParameter(txID));

        Map<String, String> map = new HashMap<String, String>();

        for (String name : jsonTransaction.names()) {
            this.decomposeJSONValue(name, jsonTransaction.get(name), map);
        }

        return map;
    }

    public synchronized String getMemoField(String acc, String txID)
            throws WalletCallException, IOException, InterruptedException {
        JsonArray jsonTransactions = this.executeCommandAndGetJsonArray(
                "z_listreceivedbyaddress", wrapStringParameter(acc));

        for (int i = 0; i < jsonTransactions.size(); i++) {
            if (jsonTransactions.get(i).asObject().getString("txid", "ERROR!").equals(txID)) {
                if (jsonTransactions.get(i).asObject().get("memo") == null) {
                    return null;
                }

                String memoHex = jsonTransactions.get(i).asObject().getString("memo", "ERROR!");
                String decodedMemo = Util.decodeHexMemo(memoHex);

                // Return only if not null - sometimes multiple incoming transactions have the same ID
                // if we have loopback send etc.
                if (decodedMemo != null) {
                    return decodedMemo;
                }
            }
        }

        return null;
    }


    public synchronized void keypoolRefill(int count)
            throws WalletCallException, IOException, InterruptedException {
        String result = this.executeCommandAndGetSingleStringResponse(
                "keypoolrefill", String.valueOf(count));
    }

    public synchronized String executeMnsyncReset() throws WalletCallException, IOException, InterruptedException {
        String result = this.executeCommandAndGetSingleStringResponse("mnsync", "reset");
        return result;
    }

    public synchronized String startAllMasternodes() throws WalletCallException, IOException, InterruptedException {
        JsonObject result = this.executeCommandAndGetJsonObject("masternode", "start-all");
        return result.toString(WriterConfig.PRETTY_PRINT);
    }

    public synchronized String startMissingMasternodes() throws WalletCallException, IOException, InterruptedException {
        JsonObject result = this.executeCommandAndGetJsonObject("masternode", "start-missing");
        return result.toString(WriterConfig.PRETTY_PRINT);
    }

    public synchronized String startMasternodeByAlias(String aliasName) throws WalletCallException, IOException, InterruptedException {
        JsonObject result = this.executeCommandAndGetJsonObject("masternode", "start-alias", aliasName);
        return result.toString(WriterConfig.PRETTY_PRINT);
    }

    public synchronized String getRawTransaction(String txID)
            throws WalletCallException, IOException, InterruptedException {
        JsonObject jsonTransaction = this.executeCommandAndGetJsonObject(
                "gettransaction", wrapStringParameter(txID));

        return jsonTransaction.toString(WriterConfig.PRETTY_PRINT);
    }


    // return UNIX time as tring
    public synchronized String getWalletTransactionTime(String txID)
            throws WalletCallException, IOException, InterruptedException {
        JsonObject jsonTransaction = this.executeCommandAndGetJsonObject(
                "gettransaction", wrapStringParameter(txID));

        return String.valueOf(jsonTransaction.getLong("time", -1));
    }


    public synchronized String getWalletTransactionConfirmations(String txID)
            throws WalletCallException, IOException, InterruptedException {
        JsonObject jsonTransaction = this.executeCommandAndGetJsonObject(
                "gettransaction", wrapStringParameter(txID));

        return jsonTransaction.get("confirmations").toString();
    }


    // Checks if a certain T address is a watch-only address or is otherwise invalid.
    public synchronized boolean isWatchOnlyOrInvalidAddress(String address)
            throws WalletCallException, IOException, InterruptedException {
        JsonObject response = this.executeCommandAndGetJsonValue("validateaddress", wrapStringParameter(address)).asObject();

        if (response.getBoolean("isvalid", false)) {
            return response.getBoolean("iswatchonly", true);
        }

        return true;
    }


    // Returns confirmed balance only!
    public synchronized String getBalanceForAddress(String address)
            throws WalletCallException, IOException, InterruptedException {
        JsonValue response = this.executeCommandAndGetJsonValue("z_getbalance", wrapStringParameter(address));

        return String.valueOf(response.toString());
    }


    public synchronized String getUnconfirmedBalanceForAddress(String address)
            throws WalletCallException, IOException, InterruptedException {
        JsonValue response = this.executeCommandAndGetJsonValue("z_getbalance", wrapStringParameter(address), "0");

        return String.valueOf(response.toString());
    }


    public synchronized String createNewAddress(boolean isZAddress)
            throws WalletCallException, IOException, InterruptedException {
        String strResponse = this.executeCommandAndGetSingleStringResponse((isZAddress ? "z_" : "") + "getnewaddress");

        return strResponse.trim();
    }


    // Returns OPID
    public synchronized String sendCash(String from, String to, String amount, String memo, String transactionFee)
            throws WalletCallException, IOException, InterruptedException {
        StringBuilder hexMemo = new StringBuilder();
        for (byte c : memo.getBytes("UTF-8")) {
            String hexChar = Integer.toHexString((int) c);
            if (hexChar.length() < 2) {
                hexChar = "0" + hexChar;
            }
            hexMemo.append(hexChar);
        }

        JsonObject toArgument = new JsonObject();
        toArgument.set("address", to);
        if (hexMemo.length() >= 2) {
            toArgument.set("memo", hexMemo.toString());
        }

        // The JSON Builder has a problem with double values that have no fractional part
        // it serializes them as integers that ZCash does not accept. So we do a replacement
        // TODO: find a better/cleaner way to format the amount
        toArgument.set("amount", "\uFFFF\uFFFF\uFFFF\uFFFF\uFFFF");

        JsonArray toMany = new JsonArray();
        toMany.add(toArgument);

        String amountPattern = "\"amount\":\"\uFFFF\uFFFF\uFFFF\uFFFF\uFFFF\"";
        // Make sure our replacement hack never leads to a mess up
        String toManyBeforeReplace = toMany.toString();
        int firstIndex = toManyBeforeReplace.indexOf(amountPattern);
        int lastIndex = toManyBeforeReplace.lastIndexOf(amountPattern);
        if ((firstIndex == -1) || (firstIndex != lastIndex)) {
            throw new WalletCallException("Error in forming z_sendmany command: " + toManyBeforeReplace);
        }

        DecimalFormatSymbols decSymbols = new DecimalFormatSymbols(Locale.ROOT);

        // Properly format teh transaction fee as a number
        if ((transactionFee == null) || (transactionFee.trim().length() <= 0)) {
            transactionFee = "0.0001"; // Default value
        } else {
            transactionFee = new DecimalFormat(
                    "########0.00######", decSymbols).format(Double.valueOf(transactionFee));
        }

        // This replacement is a hack to make sure the JSON object amount has double format 0.00 etc.
        // TODO: find a better way to format the amount
        String toManyArrayStr = toMany.toString().replace(
                amountPattern,
                "\"amount\":" + new DecimalFormat("########0.00######", decSymbols).format(Double.valueOf(amount)));

        String[] sendCashParameters = new String[]
                {
                        this.zcashcli.getCanonicalPath(), "z_sendmany", wrapStringParameter(from),
                        wrapStringParameter(toManyArrayStr),
                        // Default min confirmations for the input transactions is 1
                        "1",
                        // transaction fee
                        transactionFee
                };

        // Safeguard to make sure the monetary amount does not differ after formatting
        BigDecimal bdAmout = new BigDecimal(amount);
        JsonArray toManyVerificationArr = Json.parse(toManyArrayStr).asArray();
        BigDecimal bdFinalAmount =
                new BigDecimal(toManyVerificationArr.get(0).asObject().getDouble("amount", -1));
        BigDecimal difference = bdAmout.subtract(bdFinalAmount).abs();
        if (difference.compareTo(new BigDecimal("0.000000015")) >= 0) {
            throw new WalletCallException("Error in forming z_sendmany command: Amount differs after formatting: " +
                    amount + " | " + toManyArrayStr);
        }

        Log.info("The following send command will be issued: " +
                sendCashParameters[0] + " " + sendCashParameters[1] + " " +
                sendCashParameters[2] + " " + sendCashParameters[3] + " " +
                sendCashParameters[4] + " " + sendCashParameters[5] + ".");

        // Create caller to send cash
        CommandExecutor caller = new CommandExecutor(sendCashParameters);
        String strResponse = caller.execute();

        if (strResponse.trim().toLowerCase(Locale.ROOT).startsWith("error:") ||
                strResponse.trim().toLowerCase(Locale.ROOT).startsWith("error code:")) {
            throw new WalletCallException("Error response from wallet: " + strResponse);
        }

        Log.info("Sending cash with the following command: " +
                sendCashParameters[0] + " " + sendCashParameters[1] + " " +
                sendCashParameters[2] + " " + sendCashParameters[3] + " " +
                sendCashParameters[4] + " " + sendCashParameters[5] + "." +
                " Got result: [" + strResponse + "]");

        return strResponse.trim();
    }


    // Returns OPID
    public synchronized String sendMessage(String from, String to, double amount, double fee, String memo)
            throws WalletCallException, IOException, InterruptedException {
        String hexMemo = Util.encodeHexString(memo);
        JsonObject toArgument = new JsonObject();
        toArgument.set("address", to);
        if (hexMemo.length() >= 2) {
            toArgument.set("memo", hexMemo.toString());
        }

        DecimalFormatSymbols decSymbols = new DecimalFormatSymbols(Locale.ROOT);

        // TODO: The JSON Builder has a problem with double values that have no fractional part
        // it serializes them as integers that ZCash does not accept. This will work with the
        // fractional amounts always used for messaging
        toArgument.set("amount", new DecimalFormat("########0.00######", decSymbols).format(amount));

        JsonArray toMany = new JsonArray();
        toMany.add(toArgument);

        String toManyArrayStr = toMany.toString();
        String[] sendCashParameters = new String[]
                {
                        this.zcashcli.getCanonicalPath(), "z_sendmany", wrapStringParameter(from),
                        wrapStringParameter(toManyArrayStr),
                        // Default min confirmations for the input transactions is 1
                        "1",
                        // transaction fee
                        new DecimalFormat("########0.00######", decSymbols).format(fee)
                };

        // Create caller to send cash
        CommandExecutor caller = new CommandExecutor(sendCashParameters);
        String strResponse = caller.execute();

        if (strResponse.trim().toLowerCase(Locale.ROOT).startsWith("error:") ||
                strResponse.trim().toLowerCase(Locale.ROOT).startsWith("error code:")) {
            throw new WalletCallException("Error response from wallet: " + strResponse);
        }

        Log.info("Sending cash message with the following command: " +
                sendCashParameters[0] + " " + sendCashParameters[1] + " " +
                sendCashParameters[2] + " " + sendCashParameters[3] + " " +
                sendCashParameters[4] + " " + sendCashParameters[5] + "." +
                " Got result: [" + strResponse + "]");

        return strResponse.trim();
    }


    // Returns the message signature
    public synchronized String signMessage(String address, String message)
            throws WalletCallException, IOException, InterruptedException {
        String response = this.executeCommandAndGetSingleStringResponse(
                "signmessage", wrapStringParameter(address), wrapStringParameter(message));

        return response.trim();
    }


    // Verifies a message - true if OK
    public synchronized boolean verifyMessage(String address, String signature, String message)
            throws WalletCallException, IOException, InterruptedException {
        String response = this.executeCommandAndGetSingleStringResponse(
                "verifymessage",
                wrapStringParameter(address),
                wrapStringParameter(signature),
                wrapStringParameter(message));

        return response.trim().equalsIgnoreCase("true");
    }


    public synchronized boolean isSendingOperationComplete(String opID)
            throws WalletCallException, IOException, InterruptedException {
        JsonArray response = this.executeCommandAndGetJsonArray(
                "z_getoperationstatus", wrapStringParameter("[\"" + opID + "\"]"));
        JsonObject jsonStatus = response.get(0).asObject();

        String status = jsonStatus.getString("status", "ERROR");

        Log.info("Operation " + opID + " status is " + response + ".");

        if (status.equalsIgnoreCase("success") ||
                status.equalsIgnoreCase("error") ||
                status.equalsIgnoreCase("failed")) {
            return true;
        } else if (status.equalsIgnoreCase("executing") || status.equalsIgnoreCase("queued")) {
            return false;
        } else {
            throw new WalletCallException("Unexpected status response from wallet: " + response.toString());
        }
    }


    public synchronized boolean isCompletedOperationSuccessful(String opID)
            throws WalletCallException, IOException, InterruptedException {
        JsonArray response = this.executeCommandAndGetJsonArray(
                "z_getoperationstatus", wrapStringParameter("[\"" + opID + "\"]"));
        JsonObject jsonStatus = response.get(0).asObject();

        String status = jsonStatus.getString("status", "ERROR");

        Log.info("Operation " + opID + " status is " + response + ".");

        if (status.equalsIgnoreCase("success")) {
            return true;
        } else if (status.equalsIgnoreCase("error") || status.equalsIgnoreCase("failed")) {
            return false;
        } else {
            throw new WalletCallException("Unexpected final operation status response from wallet: " + response.toString());
        }
    }


    public synchronized String getSuccessfulOperationTXID(String opID)
            throws WalletCallException, IOException, InterruptedException {
        String TXID = null;
        JsonArray response = this.executeCommandAndGetJsonArray(
                "z_getoperationstatus", wrapStringParameter("[\"" + opID + "\"]"));
        JsonObject jsonStatus = response.get(0).asObject();
        JsonValue opResultValue = jsonStatus.get("result");

        if (opResultValue != null) {
            JsonObject opResult = opResultValue.asObject();
            if (opResult.get("txid") != null) {
                TXID = opResult.get("txid").asString();
            }
        }

        return TXID;
    }


    // May only be called for already failed operations
    public synchronized String getOperationFinalErrorMessage(String opID)
            throws WalletCallException, IOException, InterruptedException {
        JsonArray response = this.executeCommandAndGetJsonArray(
                "z_getoperationstatus", wrapStringParameter("[\"" + opID + "\"]"));
        JsonObject jsonStatus = response.get(0).asObject();

        JsonObject jsonError = jsonStatus.get("error").asObject();
        return jsonError.getString("message", "ERROR!");
    }

	public synchronized ShieldCoinbaseResponse shieldCoinbase(String from, String to)
            throws WalletCallException, IOException, InterruptedException {
        JsonObject objResponse = this.executeCommandAndGetJsonObject(
                "z_shieldcoinbase", wrapStringParameter(from),
                        wrapStringParameter(to));

		ShieldCoinbaseResponse shieldCoinbaseResponse = new ShieldCoinbaseResponse();
		shieldCoinbaseResponse.operationid = objResponse.getString("operationid", null);
		shieldCoinbaseResponse.shieldedUTXOs = objResponse.getInt("shieldedUTXOs", -1);
		if (shieldCoinbaseResponse.shieldedUTXOs == -1) {
			shieldCoinbaseResponse.shieldedUTXOs = objResponse.getInt("shieldingUTXOs", -1);
		}
		shieldCoinbaseResponse.shieldedValue = objResponse.getDouble("shieldedValue", -1);
		if (shieldCoinbaseResponse.shieldedValue == -1) {
			shieldCoinbaseResponse.shieldedValue = objResponse.getDouble("shieldingValue", -1);
		}
		shieldCoinbaseResponse.remainingUTXOs = objResponse.getInt("remainingUTXOs", -1);
		shieldCoinbaseResponse.remainingValue = objResponse.getDouble("remainingValue", -1);
        if (shieldCoinbaseResponse.shieldedValue != -1) {
            return shieldCoinbaseResponse;
        } else {
            throw new WalletCallException("Unexpected z_shieldcoinbase response from wallet: " + objResponse.toString());
        }
    }

    public synchronized NetworkAndBlockchainInfo getNetworkAndBlockchainInfo()
            throws WalletCallException, IOException, InterruptedException {
        NetworkAndBlockchainInfo info = new NetworkAndBlockchainInfo();

        String strNumCons = this.executeCommandAndGetSingleStringResponse("getconnectioncount");
        info.numConnections = Integer.valueOf(strNumCons.trim());

        String strBlockCount = this.executeCommandAndGetSingleStringResponse("getblockcount");
        info.lastBlockHeight = strBlockCount;
        String lastBlockHash = this.executeCommandAndGetSingleStringResponse("getblockhash", strBlockCount.trim());
        JsonObject lastBlock = this.executeCommandAndGetJsonObject("getblock", wrapStringParameter(lastBlockHash.trim()));
        info.lastBlockDate = new Date(Long.valueOf(lastBlock.getLong("time", -1) * 1000L));

        return info;
    }


    public synchronized void lockWallet()
            throws WalletCallException, IOException, InterruptedException {
        String response = this.executeCommandAndGetSingleStringResponse("walletlock");

        // Response is expected to be empty
        if (response.trim().length() > 0) {
            throw new WalletCallException("Unexpected response from wallet: " + response);
        }
    }


    // Unlocks the wallet for 5 minutes - meant to be followed shortly by lock!
    // TODO: tests with a password containing spaces
    public synchronized void unlockWallet(String password)
            throws WalletCallException, IOException, InterruptedException {
        String response = this.executeCommandAndGetSingleStringResponse(
                "walletpassphrase", wrapStringParameter(password), "300");

        // Response is expected to be empty
        if (response.trim().length() > 0) {
            throw new WalletCallException("Unexpected response from wallet: " + response);
        }
    }


    // Wallet locks check - an unencrypted wallet will give an error
    // zcash-cli walletlock
    // error: {"code":-15,"message":"Error: running with an unencrypted wallet, but walletlock was called."}
    public synchronized boolean isWalletEncrypted()
            throws WalletCallException, IOException, InterruptedException {
        String[] params = new String[]{this.zcashcli.getCanonicalPath(), "walletlock"};
        CommandExecutor caller = new CommandExecutor(params);
        String strResult = caller.execute();

        if (strResult.trim().length() <= 0) {
            // If it could be locked with no result - obviously encrypted
            return true;
        } else if (strResult.trim().toLowerCase(Locale.ROOT).startsWith("error:")) {
            // Expecting an error of an unencrypted wallet
            String jsonPart = strResult.substring(strResult.indexOf("{"));
            JsonValue response = null;
            try {
                response = Json.parse(jsonPart);
            } catch (ParseException pe) {
                throw new WalletCallException(jsonPart + "\n" + pe.getMessage() + "\n", pe);
            }

            JsonObject respObject = response.asObject();
            if ((respObject.getDouble("code", -1) == -15) &&
                    (respObject.getString("message", "ERR").indexOf("unencrypted wallet") != -1)) {
                // Obviously unencrupted
                return false;
            } else {
                throw new WalletCallException("Unexpected response from wallet: " + strResult);
            }
        } else if (strResult.trim().toLowerCase(Locale.ROOT).startsWith("error code:")) {
            JsonObject respObject = Util.getJsonErrorMessage(strResult);
            if ((respObject.getDouble("code", -1) == -15) &&
                    (respObject.getString("message", "ERR").indexOf("unencrypted wallet") != -1)) {
                // Obviously unencrupted
                return false;
            } else {
                throw new WalletCallException("Unexpected response from wallet: " + strResult);
            }
        } else {
            throw new WalletCallException("Unexpected response from wallet: " + strResult);
        }
    }


    /**
     * Encrypts the wallet. Typical success/error use cases are:
     * <p>
     * ./zcash-cli encryptwallet "1234"
     * wallet encrypted; Bitcoin server stopping, restart to run with encrypted wallet.
     * The keypool has been flushed, you need to make a new backup.
     * <p>
     * ./zcash-cli encryptwallet "1234"
     * error: {"code":-15,"message":"Error: running with an encrypted wallet, but encryptwallet was called."}
     *
     * @param password
     */
    public synchronized void encryptWallet(String password)
            throws WalletCallException, IOException, InterruptedException {
        String response = this.executeCommandAndGetSingleStringResponse(
                "encryptwallet", wrapStringParameter(password));
        Log.info("Result of wallet encryption is: \n" + response);
        // If no exception - obviously successful
    }


    public synchronized String backupWallet(String fileName)
            throws WalletCallException, IOException, InterruptedException {
        Log.info("Backup up wallet to location: " + fileName);
        String response = this.executeCommandAndGetSingleStringResponse(
                "backupwallet", wrapStringParameter(fileName));
        // If no exception - obviously successful
        return response;
    }


    public synchronized String exportWallet(String fileName)
            throws WalletCallException, IOException, InterruptedException {
        Log.info("Export wallet keys to location: " + fileName);
        String response = this.executeCommandAndGetSingleStringResponse(
                "z_exportwallet", wrapStringParameter(fileName));
        // If no exception - obviously successful
        return response;
    }


    public synchronized void importWallet(String fileName)
            throws WalletCallException, IOException, InterruptedException {
        Log.info("Import wallet keys from location: " + fileName);
        String response = this.executeCommandAndGetSingleStringResponse(
                "z_importwallet", wrapStringParameter(fileName));
        // If no exception - obviously successful
    }


    public synchronized String getTPrivateKey(String address)
            throws WalletCallException, IOException, InterruptedException {
        String response = this.executeCommandAndGetSingleStringResponse(
                "dumpprivkey", wrapStringParameter(address));

        return response.trim();
    }


    public synchronized String getZPrivateKey(String address)
            throws WalletCallException, IOException, InterruptedException {
        String response = this.executeCommandAndGetSingleStringResponse(
                "z_exportkey", wrapStringParameter(address));

        return response.trim();
    }


    // Imports a private key - tries both possibilities T/Z
    public synchronized String importPrivateKey(String key)
            throws WalletCallException, IOException, InterruptedException {
        // First try a Z key
        String[] params = new String[]
                {
                        this.zcashcli.getCanonicalPath(),
                        "-rpcclienttimeout=5000",
                        "z_importkey",
                        wrapStringParameter(key)
                };
        CommandExecutor caller = new CommandExecutor(params);
        String strResult = caller.execute();

        if (Util.stringIsEmpty(strResult) ||
                (!strResult.trim().toLowerCase(Locale.ROOT).contains("error"))) {
            return strResult == null ? "" : strResult.trim();
        }

        // Obviously we have an error trying to import a Z key
        if (strResult.trim().toLowerCase(Locale.ROOT).startsWith("error") &&
                (strResult.indexOf("{") != -1)) {
            // Expecting an error of a T address key
            String jsonPart = strResult.substring(strResult.indexOf("{"));
            JsonValue response = null;
            try {
                response = Json.parse(jsonPart);
            } catch (ParseException pe) {
                throw new WalletCallException(jsonPart + "\n" + pe.getMessage() + "\n", pe);
            }

            JsonObject respObject = response.asObject();
            if ((respObject.getDouble("code", +123) == -1) &&
                    (respObject.getString("message", "ERR").indexOf("wrong network type") != -1)) {
                // Obviously T address - do nothing here
            } else {
                throw new WalletCallException("Unexpected response from wallet: " + strResult);
            }
        } else if (strResult.trim().toLowerCase(Locale.ROOT).startsWith("error code:")) {
            JsonObject respObject = Util.getJsonErrorMessage(strResult);
            if ((respObject.getDouble("code", +123) == -1) &&
                    (respObject.getString("message", "ERR").indexOf("wrong network type") != -1)) {
                // Obviously T address - do nothing here
            } else {
                throw new WalletCallException("Unexpected response from wallet: " + strResult);
            }
        } else {
            throw new WalletCallException("Unexpected response from wallet: " + strResult);
        }

        // Second try a T key
        strResult = this.executeCommandAndGetSingleStringResponse(
                "-rpcclienttimeout=5000", "importprivkey", wrapStringParameter(key));

        if (Util.stringIsEmpty(strResult) ||
                (!strResult.trim().toLowerCase(Locale.ROOT).contains("error"))) {
            return strResult == null ? "" : strResult.trim();
        }

        // Obviously an error
        throw new WalletCallException("Unexpected response from wallet: " + strResult);
    }


    private JsonObject executeCommandAndGetJsonObject(String command1, String command2)
            throws WalletCallException, IOException, InterruptedException {
        return this.executeCommandAndGetJsonObject(command1, command2, null);
    }

    private JsonObject executeCommandAndGetJsonObject(String command1, String command2, String command3)
            throws WalletCallException, IOException, InterruptedException {
        JsonValue response = this.executeCommandAndGetJsonValue(command1, command2, command3);

        if (response.isObject()) {
            return response.asObject();
        } else {
            throw new WalletCallException("Unexpected non-object response from wallet: " + response.toString());
        }

    }


    private JsonArray executeCommandAndGetJsonArray(String command1, String command2)
            throws WalletCallException, IOException, InterruptedException {
        return this.executeCommandAndGetJsonArray(command1, command2, null);
    }


    private JsonArray executeCommandAndGetJsonArray(String command1, String command2, String command3)
            throws WalletCallException, IOException, InterruptedException {
        JsonValue response = this.executeCommandAndGetJsonValue(command1, command2, command3);

        if (response.isArray()) {
            return response.asArray();
        } else {
            throw new WalletCallException("Unexpected non-array response from wallet: " + response.toString());
        }
    }


    private JsonValue executeCommandAndGetJsonValue(String command1, String command2)
            throws WalletCallException, IOException, InterruptedException {
        return this.executeCommandAndGetJsonValue(command1, command2, null);
    }


    private JsonValue executeCommandAndGetJsonValue(String command1, String command2, String command3)
            throws WalletCallException, IOException, InterruptedException {
        String strResponse = this.executeCommandAndGetSingleStringResponse(command1, command2, command3);

        JsonValue response = null;
        try {
            response = Json.parse(strResponse);
        } catch (ParseException pe) {
            throw new WalletCallException(strResponse + "\n" + pe.getMessage() + "\n", pe);
        }

        return response;
    }


    private String executeCommandAndGetSingleStringResponse(String command1)
            throws WalletCallException, IOException, InterruptedException {
        return this.executeCommandAndGetSingleStringResponse(command1, null);
    }


    private String executeCommandAndGetSingleStringResponse(String command1, String command2)
            throws WalletCallException, IOException, InterruptedException {
        return this.executeCommandAndGetSingleStringResponse(command1, command2, null);
    }


    private String executeCommandAndGetSingleStringResponse(String command1, String command2, String command3)
            throws WalletCallException, IOException, InterruptedException {
        return executeCommandAndGetSingleStringResponse(command1, command2, command3, null);
    }


    private String executeCommandAndGetSingleStringResponse(
            String command1, String command2, String command3, String command4)
            throws WalletCallException, IOException, InterruptedException {
        String[] params;
        if (command4 != null) {
            params = new String[]{this.zcashcli.getCanonicalPath(), command1, command2, command3, command4};
        } else if (command3 != null) {
            params = new String[]{this.zcashcli.getCanonicalPath(), command1, command2, command3};
        } else if (command2 != null) {
            params = new String[]{this.zcashcli.getCanonicalPath(), command1, command2};
        } else {
            params = new String[]{this.zcashcli.getCanonicalPath(), command1};
        }

        CommandExecutor caller = new CommandExecutor(params);

        String strResponse = caller.execute();
        if (strResponse.trim().toLowerCase(Locale.ROOT).startsWith("error:") ||
                strResponse.trim().toLowerCase(Locale.ROOT).startsWith("error code:")) {
            throw new WalletCallException("Error response from wallet: " + strResponse);
        }

        return strResponse;
    }


    // Used to wrap string parameters on the command line - not doing so causes problems on Windows.
    public static String wrapStringParameter(String param) {
        OS_TYPE os = OSUtil.getOSType();

        // Fix is made for Windows only
        if (os == OS_TYPE.WINDOWS) {
            param = "\"" + param.replace("\"", "\\\"") + "\"";
        }

        return param;
    }


    private void decomposeJSONValue(String name, JsonValue val, Map<String, String> map) {
        if (val.isObject()) {
            JsonObject obj = val.asObject();
            for (String memberName : obj.names()) {
                this.decomposeJSONValue(name + "." + memberName, obj.get(memberName), map);
            }
        } else if (val.isArray()) {
            JsonArray arr = val.asArray();
            for (int i = 0; i < arr.size(); i++) {
                this.decomposeJSONValue(name + "[" + i + "]", arr.get(i), map);
            }
        } else {
            map.put(name, val.toString());
        }
    }

}
