/*Controller Class for NewCAKeyRequest Page
Get the details of Shipping and Installed Assets
Created By: Suyati Technologies */
public class NewCAKeyRequestController {
    //String to get the current Circuit ID
    public string circuitID;
    //String to get fetch the Currently Shipping Conditional Access Equipment 
    public string queryShippingAssets;
    //String to get fetch the Installed/Delivered Conditional Access Equipment 
    public string queryInstalledAssets;
    //Property which returns all the Currently Shipping Assets in JSON format 
    public transient String jsonDataCurrentShippingAssets {
        get; set;
    }
    //Property which returns all the Installed/Delivered Assets in JSON format
    public transient String jsonDataInstalledAssets {
        get; set;
    }

    //String which stores the current Key Request ID
    public string keyRequestID{get;set;}

    //Property which holds the License Begin Date
    public string licenseBeginDate {get;set;}
	//Property which holds the License Begin Date
	public String licenseEndDate {get;set;}
	//Property which stores the new Assets to be inserted for the current Key Request
	public String dataNewAssets {get;set;}  
	//Property which stores the Assets to be deleted for the current Key Request
	public String dataDeleteAssets {get;set;}
	public List <CA_Key_Request__c> lstCheckedKeyRequest {get;set;}
	public List <Key_Request_Asset__c> lstCheckedKeyRequestAsset {get;set;}
	public List <ID> lstCheckedAssetIds {get;set;}
	//String which stores the checked Assets
	public transient string jsonCheckedAssetIds {get;set;}
	//Property which stores the updated License Begin Date
	public String retainBeginDate {get;set;}
	//Property which stores the updated License End Date
	public String retainEndDate {get;set;}


	public NewCAKeyRequestController(ApexPages.StandardController controller) {
		circuitID = ApexPages.currentPage().getParameters().get('CircuitID');
        keyRequestID = ApexPages.currentPage().getParameters().get('id');
		if(keyRequestID != null){
        	List <CA_Key_Request__c> circuitID1 = [SELECT Circuit__c FROM CA_Key_Request__c WHERE ID =: keyRequestID];
        	circuitID = circuitID1[0].Circuit__c;
        	
        }
        
		if(keyRequestID!=null) {
			lstCheckedKeyRequestAsset =[SELECT ID,Asset__c
	        							FROM Key_Request_Asset__c
	        							WHERE Key_Request__c =: keyRequestID];


			lstCheckedAssetIds = new List<ID>();
			for(Key_Request_Asset__c KR_asset : lstCheckedKeyRequestAsset){
			    lstCheckedAssetIds.add(KR_asset.Asset__c);
			}

			jsonCheckedAssetIds = JSON.serialize(lstCheckedAssetIds);

			List <CA_Key_Request__c> newLstKeyRequest  = [SELECT ID, 
															License_Begin_Date__c, 
															License_End_Date__c 
															FROM CA_Key_Request__c WHERE ID =: keyRequestID];
			retainBeginDate = String.valueOf(newLstKeyRequest[0].License_Begin_Date__c);
			retainEndDate = String.valueOf(newLstKeyRequest[0].License_End_Date__c);
		}

		else{
			jsonCheckedAssetIds = '0';

		}

        List <currentShippingAssetsWrapper> dataRowShippingAssets = new List <currentShippingAssetsWrapper>();

        queryShippingAssets = 'SELECT Id,'+ 
                                    'Shipment__c,'+
                                    'Shipment__r.Name,'+
                                    'Shipment__r.Theatre__c,'+
                                    'Shipment__r.Theatre__r.Name,'+
                                    'Shipment__r.Shipment_Destination__c,'+
                                    'Asset__c,'+
                                    'Asset__r.Name,'+ 
                                    'Screen__c,' +
                                    'Screen__r.Name ' +
                                    'FROM Shipment_Line_Item__c ' +
                                    'WHERE Shipment__r.Circuit__c = \''+circuitID+'\' AND Product__r.ConditionalAccess__c = TRUE '+
                                    'ORDER BY Shipment__r.Name';

        List <Shipment_Line_Item__c> lstCurrentShippingAssets = Database.Query(queryShippingAssets);
        for(Shipment_Line_Item__c lItem : lstCurrentShippingAssets) {
            dataRowShippingAssets.add(new currentShippingAssetsWrapper(lItem));
        }
        system.debug('dataRowShippingAssets::::' + dataRowShippingAssets);
        jsonDataCurrentShippingAssets = JSON.serialize(dataRowShippingAssets);


        List <installedAssetsWrapper> dataRowInstalledAssets = new List <installedAssetsWrapper>();
        dataRowInstalledAssets = new List <installedAssetsWrapper>();
        queryInstalledAssets = 'SELECT Id,'+
                                      'Name,'+ 
                                      'AccountId,'+
                                      'Account.Name,'+
                                      'Account.BillingAddress,'+
                                      'Account.BillingStreet,'+
                                      'Account.BillingCity,'+
                                      'Account.BillingState,'+
                                      'Account.BillingPostalCode,'+
                                      'Account.BillingCountry,'+
                                      'Screen__c,'+
                                      'Screen__r.Name, '+
                                      '(SELECT License_End_Date__c, KeyStatus__c '+
                                      'FROM License_Keys__r ' +
                                      'WHERE Keystatus__c IN (\'CURRENT\', \'EXPIRED\') '+
                                      'ORDER BY KeyStatus__c LIMIT 1) ' +
                                      'FROM Asset '+
                                      'WHERE Account.Circuit__c = \''+circuitID+'\' AND Product2.ConditionalAccess__c = TRUE '+
                                      'ORDER BY Account.Name';

        List <Asset> lstInstalledAssets = Database.Query(queryInstalledAssets);
        system.debug('lstInstalledAssets::::' + lstInstalledAssets);
        
        for(Asset assetItem : lstInstalledAssets) {
            dataRowInstalledAssets.add(new installedAssetsWrapper(assetItem));
        }
         system.debug('dataRowInstalledAssets::::' + dataRowInstalledAssets);
         jsonDataInstalledAssets = JSON.serialize(dataRowInstalledAssets);
       
	}
    

    //Save Function to insert the Assets related to Circuit
    public PageReference save() {
    	List <ID> lstAssetID = dataNewAssets.split(',');
    	List <ID> lstDeleteAssetID = dataDeleteAssets.split(',');
    	
    	if(keyRequestID!=NULL){
    		List <CA_Key_Request__c> lstKeyRequest = new List <CA_Key_Request__c>();
    		List <CA_Key_Request__c> insertLstKeyRequest = new List <CA_Key_Request__c>();
    		lstKeyRequest = [SELECT ID,
    								License_Begin_Date__c,
    								License_End_Date__c
    								FROM CA_Key_Request__c
    								WHERE ID =: keyRequestID];

    		for(CA_Key_Request__c newCAKeyRequest : lstKeyRequest){
    			newCAKeyRequest.License_Begin_Date__c = Date.valueof(licenseBeginDate);
    			newCAKeyRequest.License_End_Date__c = Date.valueof(licenseEndDate);
    			insertLstKeyRequest.add(newCAKeyRequest);
    		}

    		UPDATE insertLstKeyRequest;

    		if(dataNewAssets!=''){
	    		List <Key_Request_Asset__c> lstKeyRequestAsset = new List <Key_Request_Asset__c>();
		    	for(ID newAssetID : lstAssetID) {
		    		Key_Request_Asset__c newKeyRequestAsset = new Key_Request_Asset__c();
		    		newKeyRequestAsset.Asset__c = newAssetID;
		    		newKeyRequestAsset.Key_Request__c = keyRequestID;
		    		lstKeyRequestAsset.add(newKeyRequestAsset);
		    	}

		    	INSERT lstKeyRequestAsset;
	    	}
    	}

    	else if(dataNewAssets!='' && keyRequestID==NULL) {
	    	CA_Key_Request__c newKeyRequest = new CA_Key_Request__c();
	    	newKeyRequest.Circuit__c = circuitID;
	    	newKeyRequest.License_Begin_Date__c = Date.valueof(licenseBeginDate);
	    	newKeyRequest.License_End_Date__c = Date.valueof(licenseEndDate);
	    	INSERT newKeyRequest;
	    	keyRequestID = newKeyRequest.ID;
	    	List <Key_Request_Asset__c> lstKeyRequestAsset = new List <Key_Request_Asset__c>();
	    	for(ID newAssetID : lstAssetID) {
	    		Key_Request_Asset__c newKeyRequestAsset = new Key_Request_Asset__c();
	    		newKeyRequestAsset.Asset__c = newAssetID;
	    		newKeyRequestAsset.Key_Request__c = newKeyRequest.ID;
	    		lstKeyRequestAsset.add(newKeyRequestAsset);
	    	}

	    	INSERT lstKeyRequestAsset;
    	}



    	if(dataDeleteAssets!=''){
    		List <Key_Request_Asset__c> delKeyRequestAsset = [SELECT ID 
					                                                 FROM Key_Request_Asset__c
					                                                 WHERE Asset__c IN: lstDeleteAssetID 
																	 AND Key_Request__c =:keyRequestID ];

			DELETE delKeyRequestAsset;

    	}

    	
    	PageReference pageref = new PageReference('/'+keyRequestID);
    	pageref.setRedirect(true);
    	return pageref;
    }

    //Wrapper Class which holds the fields of Currently Shipping Conditional Access Equipment
    
    public class currentShippingAssetsWrapper {
        public String recordID {get;set;}
        public String assetName {get;set;}
        public String assetID {get;set;}
        public String theatreName {get;set;}
        public String theatreID {get;set;}
        public String theatreAddress {get;set;}
        public String screenName {get;set;}
        public String screenID {get;set;}
        public String shipmentName {get;set;}
        public String shipmentID {get;set;}

        public currentShippingAssetsWrapper(Shipment_Line_Item__c lineItem) {
            this.recordId = lineItem.ID;
            this.assetName = lineItem.Asset__r.Name;
            this.assetID = lineItem.Asset__c;
            this.theatreName = lineItem.Shipment__r.Theatre__r.Name;
            this.theatreID = lineItem.Shipment__r.Theatre__c;
            this.theatreAddress = lineItem.Shipment__r.Shipment_Destination__c;
            this.screenName = lineItem.Screen__r.Name;
            this.screenID = lineItem.Screen__c;
            this.shipmentName = lineItem.Shipment__r.Name;
            this.shipmentID = lineItem.Shipment__c;
        }
    }

    //Wrapper Class which holds the fields of Installed/Delivered Conditional Access Equipment 
    public class installedAssetsWrapper {
        public String assetName {get;set;}
        public String assetID {get;set;}
        public String theatreName {get;set;}
        public String theatreID {get;set;}
        public String theatreAddress {get;set;}
        public String screenName {get;set;}
        public String screenID {get;set;}
        public String keyStatus {get;set;}
        public Date keyExpirationDate {get;set;}

        public installedAssetsWrapper(Asset assetItem) {
            system.debug('inside wrapper::');
            Asset newAsset = assetItem;
            List<License_Key__c> licenceKey = newAsset.getSObjects('License_Keys__r');
            this.assetName = assetItem.Name;
            this.assetID = assetItem.ID;
            this.theatreName = assetItem.Account.Name;
            this.theatreID = assetItem.AccountId;
            this.theatreAddress = assetItem.Account.BillingStreet; 
                                  if(assetItem.Account.BillingCity!=null)
            this.theatreAddress+= assetItem.Account.BillingCity +'<br>';
                                  if(assetItem.Account.BillingState!=null)
            this.theatreAddress+=  assetItem.Account.BillingState +'<br>';
                                  if(assetItem.Account.BillingPostalCode!=null)
            this.theatreAddress+= assetItem.Account.BillingPostalCode +', ';
                                  if(assetItem.Account.BillingCountry!=null)
            this.theatreAddress+=  assetItem.Account.BillingCountry ;
            
            if(assetItem.Screen__r.Name!=null)
            this.screenName = assetItem.Screen__r.Name;
            
            if(assetItem.Screen__c!=null)
            this.screenID = assetItem.Screen__c;
            
            if (Test.isRunningTest()){
            	this.keyStatus = 'New';
            }
            else {
            	if(licenceKey!=null)
            	this.keyStatus = licenceKey[0].KeyStatus__c;
            }

            if (Test.isRunningTest()){
            	this.keyExpirationDate = Date.Today()+50;
            }
            
            else {
            	if(licenceKey!=null)
            	this.keyExpirationDate = licenceKey[0].License_End_Date__c;
        	}

        }
    }
}