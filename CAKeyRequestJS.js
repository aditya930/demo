var j$ = jQuery.noConflict();
       j$(document).ready(function() {
        	fetchAssetArr();
        });
        var newAssetArr = [];
        var shippingAssetArr = [];
        var installedAssetArr = [];
        var deleteAssetArr = [];
        var retainAssetArr = [];
        j$('[class~=jqueryDate]').datepicker({                    
                  changeMonth: true,
                  changeYear: true,
                  showOn: "button",      
                  buttonImage: "//jqueryui.com/resources/demos/datepicker/images/calendar.gif",
                  buttonImageOnly: true,
                  buttonText: "Select date",
                  minDate:"-100y",
                  maxDate: '+100y',
                  dateFormat: 'yy-mm-dd'                 
            });
        buildShippingAssetTable();
        buildInstalledAssetTable();

        
        //Click Function which fetches all the Shipping Assets
        j$('.selectAllShipAssets').click(function() {
            if(j$('.selectAllShipAssets').prop('checked') ){
            	shippingAssetArr.length=0;
                j$('.rowIdentifierShipped').each(function(i,obj) {
                    if(j$(this).prop('checked') == false){
						j$(this).prop('checked',true);
	                    var id = j$(this).attr('id');
	                    if(id!='null')
	                    shippingAssetArr.push(id);
	            	}
                });
            }

            else {
                j$('.rowIdentifierShipped').each(function(i,obj) {
                    j$(this).prop('checked',false);
                    var assetID = j$(this).attr('id');
                    var checkCheckboxRetainAsset = retainAssetArr.indexOf(assetID);
                    if(checkCheckboxRetainAsset>=0){
                		retainAssetArr.splice(checkCheckboxRetainAsset,1);
                		deleteAssetArr.push(assetID);
                	}
                });
                shippingAssetArr.length=0;
            }

          });

        
        //Click Function which fetches all the Installed Assets
        j$('.selectAllInstalledAssets').click(function() {
            if(j$('.selectAllInstalledAssets').prop('checked')){
            	installedAssetArr.length=0;
                j$('.rowIdentifierInstalled').each(function(i,obj) {
                    if(j$(this).prop('checked') == false){
	                    j$(this).prop('checked',true);
	                    var id = j$(this).attr('id');
	                    if(id!='null')
	                    installedAssetArr.push(id);
	            	}
                });
            }

            else {
                j$('.rowIdentifierInstalled').each(function(i,obj) {
                    j$(this).prop('checked',false);
                    var assetID = j$(this).attr('id');
                    var checkCheckboxRetainAsset = retainAssetArr.indexOf(assetID);
                    if(checkCheckboxRetainAsset>=0){
                		retainAssetArr.splice(checkCheckboxRetainAsset,1);
                		deleteAssetArr.push(assetID);
                	}
                });
                installedAssetArr.length=0;
            }

         });

        
        //Date Check Validation
        j$('.rangeTo').change(function() {
                    if(j$('.rangeFrom').val() > j$('.rangeTo').val()){
                        j$('.rangeFrom').val('');
                        j$('.rangeTo').val('');
                        j$('#errorMsgDate').html('Please select a date greater than from date');
                    } else{
                        j$('#errorMsgDate').html('');
                    }
                });

        //Uncheck all selected in the previous page when going to next page Shipment Assets
        j$('#tblShippingAssets').on( 'page.dt', function () {
                   j$('.selectAllShipAssets').prop('checked',false);
                      window.setTimeout(repopulateCheckboxes,10);
                  });

        //Uncheck all selected in the previous page when going to next page Installed Assets
        j$('#tblInstalledAssets').on( 'page.dt', function () {
                   j$('.selectAllInstalledAssets').prop('checked',false);
                      window.setTimeout(repopulateCheckboxes,10);
                      
                    });


        //Uncheck all selected checkbox when the number of entries change in Shipping Assets
        j$('select[name=tblShippingAssets_length]').change(function(){
            j$('.selectAllShipAssets').prop('checked',false);
          });

        
        //Uncheck all selected checkbox when the number of entries change in Installed Assets
        j$('select[name=tblInstalledAssets_length]').change(function(){
            j$('.selectAllInstalledAssets').prop('checked',false);
          });


        function uncheckNextPageShip(){
        	j$('.selectAllShipAssets').click();
            j$('.selectAllShipAssets').click(); 
        }

        function uncheckNextPageInstalled(){
        	j$('.selectAllInstalledAssets').click();
            j$('.selectAllInstalledAssets').click(); 
        }
        //Javascript Function to build Shipping Assets Table
        function buildShippingAssetTable(){
            var allShippingAssets = {!jsonDataCurrentShippingAssets};
            j$.each(allShippingAssets, function() {

            this.id = '<input id="' + this.assetID + '" class="rowIdentifierShipped"' ;
            this.id	+='type="checkbox" onchange="selectCheckboxShip(\''+this.assetID+'\')"/>';

                if(this.assetName == null){  
                    this.assetName = '';
                } else {
                    this.assetName = '<a href="/' + this.assetID + '" target="_blank">' + this.assetName + '</a>';
                }
                
                if(this.theatreName == null){  
                    this.theatreName = '';
                } else {
                    this.theatreName = '<a href="/' + this.theatreID + '" target="_blank">' + this.theatreName + '</a>';
                }
                
                if(this.screenName == null) {
                    this.screenName = '';
                } else {
                    this.screenName = '<a href="/' + this.screenID + '" target="_blank">' + this.screenName + '</a>';
                }

                if(this.shipmentName == null) {
                    this.shipmentName = '';
                } else {
                    this.shipmentName = '<a href="/' + this.shipmentID + '" target="_blank">' + this.shipmentName + '</a>';
                }

            });

            j$('#tblShippingAssets').dataTable({
                    
                    "aaData": allShippingAssets,
                    "iDisplayLength": 10,
                    "aoColumns": [{
                        "mData": "id",
                        "bSortable": false,
                        "aTargets": [1]
                    }, {
                        "mData": "assetName"
                    }, {
                        "mData": "theatreName"
                    }, {
                        "mData": "theatreAddress"
                    }, {
                        "mData": "screenName"
                    }, {
                        "mData": "shipmentName"
                    }]
                    
                });
        }

        //Javascript Function to build Installed Assets Table
        function buildInstalledAssetTable() {
            var allInstalledAssets = {!jsonDataInstalledAssets};
            j$.each(allInstalledAssets, function() {

            this.id = '<input id="' + this.assetID + '" class="rowIdentifierInstalled" type="checkbox"' ;
            this.id+= 'onchange="selectCheckboxInstalled(\''+this.assetID+'\')"/>';


                if(this.assetName == null){  
                    this.assetName = '';
                } else {
                    this.assetName = '<a href="/' + this.assetID + '" target="_blank">' + this.assetName + '</a>';
                }
                
                if(this.theatreName == null){  
                    this.theatreName = '';
                } else {
                    this.theatreName = '<a href="/' + this.theatreID + '" target="_blank">' + this.theatreName + '</a>';
                }
                
                if(this.screenName == null) {
                    this.screenName = '';
                } else {
                    this.screenName = '<a href="/' + this.screenID + '" target="_blank">' + this.screenName + '</a>';
                }

           });

            j$('#tblInstalledAssets').dataTable({
                    
                    "aaData": allInstalledAssets,
                    "iDisplayLength": 10,
                    "aoColumns": [{
                        "mData": "id",
                        "bSortable": false,
                        "aTargets": [1]
                    }, {
                        "mData": "assetName"
                    }, {
                        "mData": "theatreName"
                    }, {
                        "mData": "theatreAddress"
                    }, {
                        "mData": "screenName"
                    }, {
                        "mData": "keyStatus"
                    }, {
                        "mData": "keyExpirationDate",
                        "type": "date"
                    }]
                    
            });

        }

        function selectCheckboxShip(assetID) {
            if(j$('#' + assetID).prop('checked') && assetID!='null') {
                shippingAssetArr.push(assetID);
                
            }

            else {
                var checkCheckboxNewAsset = shippingAssetArr.indexOf(assetID);
                var checkCheckboxRetainAsset = retainAssetArr.indexOf(assetID);

                if(checkCheckboxRetainAsset>=0){
                	retainAssetArr.splice(checkCheckboxRetainAsset,1);
                	deleteAssetArr.push(assetID);
                }
                if(checkCheckboxNewAsset>=0){
                    shippingAssetArr.splice(checkCheckboxNewAsset,1);
                }
            }

        }

        function selectCheckboxInstalled(assetID) {
            if(j$('#' + assetID).prop('checked') && assetID!='null') {
                installedAssetArr.push(assetID);
                
            }

            else {
                var checkCheckboxInstallAsset = installedAssetArr.indexOf(assetID);
                var checkCheckboxRetainAsset = retainAssetArr.indexOf(assetID);

                if(checkCheckboxRetainAsset>=0){
                	retainAssetArr.splice(checkCheckboxRetainAsset,1);
                	deleteAssetArr.push(assetID);
                }

                if(checkCheckboxInstallAsset>=0){
                    installedAssetArr.splice(checkCheckboxInstallAsset,1);
                }
            }


        }

        function assetToController(){

            j$.merge(newAssetArr,shippingAssetArr);
        	j$.merge(newAssetArr,installedAssetArr);
			j$.merge(retainAssetArr,newAssetArr);
        	
            if(j$('.rangeFrom').val() == '' && j$('.rangeTo').val() == ''){
                j$('#errorMsgDate').html('Please select the License Begin Date and End Date');
            }

            else if(j$('.rangeFrom').val() == ''){
                j$('#errorMsgDate').html('Please select the License Begin Date');
            }

            else if(j$('.rangeTo').val() == ''){
                j$('#errorMsgDate').html('Please select the License End Date');
            }

            else if(newAssetArr.length == 0 || deleteAssetArr.length == 0){
                j$('#errorMsgAsset').html('No Asset Selected/Deselected! Please Select/Deselect the Assets to continue');
            }

            else {
	            j$('input[id$=hiddenBlockNewAssets]').val(newAssetArr);
	            j$('input[id$=hiddenBlockDeleteAssets]').val(deleteAssetArr);
	            actFuncAssetToController();
            }
        }

        function xyz(){
        	j$.merge(newAssetArr,shippingAssetArr);
        	j$.merge(newAssetArr,installedAssetArr);

        	j$.merge(retainAssetArr,newAssetArr);
        	
        }

        function fetchAssetArr(){
        	
        	retainAssetArr = {!jsonCheckedAssetIds};
        	if(retainAssetArr == '0'){
        		retainAssetArr=[];
        	}
			else{
			repopulateCheckboxes();
			}
			j$('.rangeFrom').val('{!retainBeginDate}');
			j$('.rangeTo').val('{!retainEndDate}');
        }

        function repopulateCheckboxes(){
        	 j$(retainAssetArr).each(function(i,val){
                j$('#'+ val).prop('checked',true);
             });
        }
        
        