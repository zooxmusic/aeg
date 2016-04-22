# ISM FTP/SFTP Transfer

The IMS FTP/SFTP Transfers is an application that is scheduled in al three IMS environments to transfer program files from and processed files to our partners

Scheduled Windows Tasks
=============

These transfer scripts are configured to run as Windows Scheduled Tasks every 30 min on the hour for inbound and on the 15/45 for outbound.

Current Status
=============

Latest Release: Not releases as of 4/13/2016

How to use
=============

+ Windows 7

       Start -> Control Panel -> Administrative Tools -> Services

+ Window Server 2012

      (right click) Windows Start. Then choose Control Panel

You should see the folders AEG//Transfer.  When the "Transfer" folder is selected, the pages are populated

Configuration
=============
+ The basic pattern to call the transfer scripts are as follows;

        <path_to_java> -jar <path_to_jar>/transfer.jar $direction $partner

        // Use this to transfer both directions for all partners
        example: E:/AEG/scritps/transfer.jar

        // Use this to transfer both directions for CleaResult
        example: E:/AEG/scritps/transfer.jar both cr

        // Use this to transfer inbound directions for AEG
        example: E:/AEG/scritps/transfer.jar in aeg

The 2 variables that can be dynamically changed;
+ direction ( defaults to both)
+ partner-name (defaults to null which means all)

If you do not pass a variable for either the scripts defaults to both directions for all partners.

On Demand
=============

You are able to select a script, right click and choose Run to execute the selected script now.


Folder Mappings
=============
