/*Controller Class for ShipmentSearch Visualforce Page
Get the details of Shipment Items*/
public class ShipmentCompletionController {

    //Map property to get Shipment Items and corresponding Ids
    public transient Map < Id, Shipment_Line_Item__c > shipmentLineItemMap {
        get;
        set;
    } 
    //Property to get the from date
    public String rangeFromDate {
        get;
        set;
    } 
    //Property to get the to date
    public String rangeToDate {
        get;
        set;
    } 

    //Property to get Source Warehouse ID
    public string selectedWarehouseId {
        get;
        set;
    } 
    //SOQL query string 
    public string query; 
    // String which holds Sourcewarehouse id to be filtered
    public string filterQuery; 
    // String which holds From and To Date to be filtered
    public string dateFilterQuery; 

    //Property which returns all the Shipment records in JSON format
    public transient String jsonDataAllLineItems {
        get; set;
    }

    //Constructor to get the Warehouseid, dates from URL

    public ShipmentCompletionController() {
        filterQuery = '';
        dateFilterQuery = '';
        string fromDate = Apexpages.currentpage().getparameters().get('dFrom');
        string toDate = Apexpages.currentpage().getparameters().get('dTo');
        rangeFromDate = fromDate;
        rangeToDate = toDate;
        selectedWarehouseId = Apexpages.currentpage().getparameters().get('id');
        List < LineItemWrapper > dataRows = new List < LineItemWrapper > ();
        
        if(fromDate == NULL && toDate == NULL){
            jsonDataAllLineItems = JSON.serialize(dataRows);
            return;
        }
    
        if ((fromDate != null && fromDate != '' && toDate != null && toDate != '') && (selectedWarehouseId != null && selectedWarehouseId != '')) {
            
            filterQuery = ' WHERE SourceWarehouse__c = \'' + selectedWarehouseId + '\''+ ' AND Line_Status__c IN (\'Scheduled\', \'Received by Fulfillment Warehouse\')';
            dateFilterQuery = ' AND Shipment__r.Scheduled_Ship_Date__c >= ' + fromDate + ' AND Shipment__r.Scheduled_Ship_Date__c <= ' + toDate;

        } else if (fromDate != null && fromDate != '' && toDate != null && toDate != '') {
            
            dateFilterQuery = ' WHERE Shipment__r.Scheduled_Ship_Date__c >= ' + fromDate + ' AND Shipment__r.Scheduled_Ship_Date__c <= ' + toDate + 'AND Line_Status__c IN (\'Scheduled\', \'Received by Fulfillment Warehouse\')';
            
        } else if (selectedWarehouseId != null && selectedWarehouseId != '') {
            filterQuery = ' WHERE SourceWarehouse__c = \'' + selectedWarehouseId + '\'' + 'AND Line_Status__c IN (\'Scheduled\', \'Received by Fulfillment Warehouse\')';
        }

        query = 'SELECT Id, Shipment__r.Name, Shipment__r.MAS_Customer_Number__c, Shipment__r.Scheduled_Ship_Date__c, Shipment__r.Shipment_Type__c, Shipment__r.ShipTo__c, Shipment__r.Shipment_Destination__c, SourceWarehouse__r.Name, SourceWarehouse_ID__c, ShipDate__c, TrackingNumber__c, Line_Status__c, Product_Name_ID__c, Quantity__c, SerialNumber__c FROM Shipment_Line_Item__c' + filterQuery + dateFilterQuery;

        if(fromDate == '' && toDate == '' && selectedWarehouseId == ''){
            query = 'SELECT Id, Shipment__r.Name, Shipment__r.MAS_Customer_Number__c, Shipment__r.Scheduled_Ship_Date__c, Shipment__r.Shipment_Type__c, Shipment__r.ShipTo__c, Shipment__r.Shipment_Destination__c, SourceWarehouse__r.Name, SourceWarehouse_ID__c, ShipDate__c, TrackingNumber__c, Line_Status__c, Product_Name_ID__c, Quantity__c, SerialNumber__c FROM Shipment_Line_Item__c WHERE Line_Status__c IN (\'Scheduled\', \'Received by Fulfillment Warehouse\') LIMIT 10000';
        }
       
       
        List < Shipment_Line_Item__c > lineitems = Database.Query(query);

        for (Shipment_Line_Item__c lItem: lineitems) {
            dataRows.add(new LineItemWrapper(lItem));
        }
        jsonDataAllLineItems = JSON.serialize(dataRows);
        
    }

    //Function to select the Warehouse from a picklist
    public List < SelectOption > getWarehouseIds() {

        List < SelectOption > warehouseList = new List < SelectOption > ();
        warehouseList.add(new SelectOption('', 'None'));
        for (Warehouse__c wHouse: [SELECT Id, Name FROM Warehouse__c WHERE Warehouse_Type__c IN('RealD Distribution', 'RealD Used') AND Include_in_Inventory__c = TRUE ORDER BY Name ASC]) {
            warehouseList.add(new SelectOption(wHouse.Id, wHouse.Name));
        }
        return warehouseList;
    }


    //Property to return editable fields in JSON format
    public String jsonDataMinimal {
        get {
            if (jsonDataMinimal == null) {
                List < SmallDataRow > dataRows = new List < SmallDataRow > ();
                for (Shipment_Line_Item__c ship: [SELECT Id,
                Name,
                ShipDate__c,
                TrackingNumber__c,
                Line_Status__c,
                SerialNumber__c
                FROM Shipment_Line_Item__c  LIMIT 10000 ]) {
                    dataRows.add(new SmallDataRow(ship));
                }
                jsonDataMinimal = JSON.serialize(dataRows);
            }
            return jsonDataMinimal;
        }
        set;
    }

    //Function to store all the Shipment Line Items in a Map
    public void allLineItems() {
        List < Shipment_Line_Item__c > allLineItems = [SELECT
        Id,
        Shipment__r.Name,
        Shipment__r.MAS_Customer_Number__c,
        Shipment__r.Scheduled_Ship_Date__c,
        Shipment__r.Shipment_Type__c,
        Shipment__r.ShipTo__c,
        Shipment__r.Shipment_Destination__c,
        SourceWarehouse__r.Name,
        SourceWarehouse_ID__c,
        ShipDate__c,
        TrackingNumber__c,
        Line_Status__c,
        Product_Name_ID__c,
        Quantity__c,
        SerialNumber__c
        FROM
        Shipment_Line_Item__c];
        shipmentLineItemMap = new Map < Id, Shipment_Line_Item__c > (allLineItems);
    }


    //Function to save the updated records in the Org
    public void save() {
        Apexpages.Message msg = new Apexpages.Message(Apexpages.Severity.CONFIRM, 'Shipment changes saved successfully');
        Apexpages.Message error_msg = new Apexpages.Message(Apexpages.Severity.CONFIRM, 'An error occured while saving Shipment changes. Please contact your admin.');

        List < Shipment_Line_Item__c > shipmentsToUpdate = new List < Shipment_Line_Item__c > ();
        List < SmallDataRow > smallTableRow = (List < SmallDataRow > ) JSON.deserialize(jsonDataMinimal, List < SmallDataRow > .class);
        for (SmallDataRow sdr: smallTableRow) {

            if (shipmentLineItemMap == null) {
                this.allLineItems();
            }
            shipmentLineItemMap.get(sdr.recordId).ShipDate__c = sdr.shipDate;
            shipmentLineItemMap.get(sdr.recordId).TrackingNumber__c = sdr.trackingNumber;
            shipmentLineItemMap.get(sdr.recordId).Line_Status__c = sdr.lineStatus;
            shipmentLineItemMap.get(sdr.recordId).SerialNumber__c = sdr.serialNumber;

            shipmentsToUpdate.add(shipmentLineItemMap.get(sdr.recordId));



        }
        try {
            update shipmentsToUpdate;
            Apexpages.addMessage(msg);
        } catch (Exception e) {
            Apexpages.addMessage(error_msg);
        }
    }


    //Wrapper Class to get editable fields so that it can be updated
    public class SmallDataRow {
        public Date shipDate {
            get;
            set;
        }
        public String trackingNumber {
            get;
            set;
        }
        public String lineStatus {
            get;
            set;
        }
        public String serialNumber {
            get;
            set;
        }
        public Id recordId {
            get;
            set;
        }

        public SmallDataRow(Shipment_Line_Item__c ship) {
            this.shipDate = ship.ShipDate__c;
            this.trackingNumber = ship.TrackingNumber__c;
            this.lineStatus = ship.Line_Status__c;
            this.serialNumber = ship.SerialNumber__c;
            this.recordId = ship.Id;
        }
    }

    //Wrapper Class to all the fields of the Shipment Line Item Object 

    public class LineItemWrapper {
        public Id recordId {
            get;
            set;
        }
        public String shipmentName {
            get;
            set;
        }
        public String customerNumber {
            get;
            set;
        }
        public Date scheduledShipDate {
            get;
            set;
        }
        public String shipmentType {
            get;
            set;
        }
        public String shipTo {
            get;
            set;
        }
        public String shipDestination {
            get;
            set;
        }
        public String sourceWarehouseName {
            get;
            set;
        }
        public String sourceWarehouseId {
            get;
            set;
        }
        public Date shipDate {
            get;
            set;
        }
        public String trackingNumber {
            get;
            set;
        }
        public String lineStatus {
            get;
            set;
        }
        public String productNameId {
            get;
            set;
        }
        public Decimal quantity {
            get;
            set;
        }
        public String serialNumber {
            get;
            set;
        }

        public LineItemWrapper(Shipment_Line_Item__c lItem) {
            this.recordId = lItem.Id;
            this.shipmentName = lItem.Shipment__r.Name;
            this.customerNumber = lItem.Shipment__r.MAS_Customer_Number__c;
            this.scheduledShipDate = lItem.Shipment__r.Scheduled_Ship_Date__c;
            this.shipmentType = lItem.Shipment__r.Shipment_Type__c;
            this.shipTo = lItem.Shipment__r.ShipTo__c;
            this.shipDestination = lItem.Shipment__r.Shipment_Destination__c;
            this.sourceWarehouseName = lItem.SourceWarehouse__r.Name;
            this.sourceWarehouseId = lItem.SourceWarehouse_ID__c;
            this.shipDate = lItem.ShipDate__c;
            this.trackingNumber = lItem.TrackingNumber__c;
            this.lineStatus = lItem.Line_Status__c;
            this.productNameId = lItem.Product_Name_ID__c;
            this.quantity = lItem.Quantity__c;
            this.serialNumber = lItem.SerialNumber__c;
        }
    }

}