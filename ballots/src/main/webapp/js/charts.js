charts= Class.create( {

    initialize : function(type,array) {
    		this.type=type;
    	
    	
    	  // Load the Visualization API and the piechart package.
          google.load('visualization', '1.0', {'packages':['corechart']});

          // Set a callback to run when the Google Visualization API is loaded.
           google.setOnLoadCallback(drawChart);

          // Callback that creates and populates a data table,
          // instantiates the pie chart, passes in the data and
          // draws it.
          function drawChart() {

            // Create the data table.
            var data = new google.visualization.DataTable();
            if(type=="MAY")
            {
            	data.addColumn('string', 'Option');
                data.addColumn('number', 'Voto');
            }
            if(type=="KEM")
            {
            	data.addColumn('string', 'Kemeny Option');
                data.addColumn('number', 'Puntuación');
            }
            if(type=="BOR")
            {
            	data.addColumn('string', 'Option');
                data.addColumn('number', 'Voto');
            }
            if(type=="RANGE")
            {
            	data.addColumn('string', 'Option');
                data.addColumn('number', 'Voto');
            }
            var firstItem=true;
          
            array.forEach(function(obj)
            {
	            if(firstItem==true)
	           	{
	           		firstItem=false;
	           	}
	            else
	            {
	            	data.addRow([obj.option,obj.value])
	            }
            });
            
            // Set chart options
            var options = {'title':array[0].title,
                           'width':1000,
                           'height':600
                           ,'is3D':true
                           };

            // Instantiate and draw our chart, passing in some options.
            var chart;
            if(type=="MAY")
            {
            	chart = new google.visualization.PieChart(document.getElementById('chart_div'));
            }
            if(type=="KEM")
            {
            	chart= new google.visualization.BarChart(document.getElementById('chart_div'));
            }
            if(type=="BOR")
            {
            	chart = new google.visualization.PieChart(document.getElementById('chart_div'));
            }
            if(type=="RANGE")
            {
            	chart = new google.visualization.PieChart(document.getElementById('chart_div'));
            }
            
            chart.draw(data, options);
           
          }
    }
    

} )

Tapestry.Initializer.charts_may = function(spec) {
    var array=JSON.parse(spec);
	new charts("MAY",array);
}


Tapestry.Initializer.charts_kem = function(spec) {
	var array=JSON.parse(spec);
	new charts("KEM",array);
    
}

Tapestry.Initializer.charts_bor = function(spec) {
	
	var array=JSON.parse(spec);
	new charts("BOR",array);
}
Tapestry.Initializer.charts_range = function(spec) {
	
	var array=JSON.parse(spec);
	new charts("RANGE",array);
}
