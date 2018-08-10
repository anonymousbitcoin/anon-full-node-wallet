#!bin/sh

# ------------------------------------------------------------------
# [Niels Buekers]	Clean blockchain folders
#          			Deletes existing blockchain from user's default
#					installation directory. Should only be used in
#					case of a corrupted chainstate. User will need
#					to re-download entire blockchain if he proceeds.
#
#					'wallet', 'peers', and 'config' remain untouched.
# ------------------------------------------------------------------

echo "This operation will remove your current blockchain (blocks and chainstate)."
echo "You will need to re-download (sync) it from scratch."
echo "Only use this in case of a corrupted chainstate."

read -p "Are you sure you want to delete your entireAnonymousblockchain? (y/N) " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
	echo "Deleting ~/Library/Application Support/Anon/blocks"
    rm -rf ~/Library/Application Support/Anon/blocks
    echo "Deleting ~/Library/Application Support/Anon/chainstate"
    rm -rf ~/Library/Application Support/Anon/chainstate
    echo "Success!"
else
	echo "Aborted."
fi
