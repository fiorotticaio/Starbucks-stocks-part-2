import CanvasJSReact from '@canvasjs/react-charts';
import React from 'react';
//var CanvasJSReact = require('@canvasjs/react-charts');

var CanvasJS = CanvasJSReact.CanvasJS;
var CanvasJSChart = CanvasJSReact.CanvasJSChart;

interface GraphProps {
    web_price : number[],
    api_price : number[],
    merged_price : number[],
}


export default function Graph({web_price, api_price, merged_price}:GraphProps) {
    let web_price_dataPoints = web_price.map((item, index)=>{
        return {x: index, y: item}
    })
    let api_price_dataPoints = api_price.map((item, index)=>{
        return {x: index, y: item}
    })
    let merged_price_dataPoints = merged_price.map((item, index)=>{
        return {x: index, y: item}
    })
    let chart = new CanvasJS.Chart("chartContainer", {
        zoomEnabled: true,
        title: {
            text: "Stock Price of Starbucks"
        },
        axisX: {
            title: "chart updates every sec"
        },
        axisY:{
            prefix: "$"
        }, 
        toolTip: {
            shared: true
        },
        legend: {
            cursor:"pointer",
            verticalAlign: "top",
            fontSize: 22,
            fontColor: "dimGrey",
            itemclick : toggleDataSeries
        },
        data: [
            { 
                type: "line",
                xValueType: "dateTime",
                yValueFormatString: "$####.00",
                xValueFormatString: "hh:mm:ss TT",
                showInLegend: true,
                name: "API price",
                dataPoints: api_price_dataPoints
            },
            {				
                type: "line",
                xValueType: "dateTime",
                yValueFormatString: "$####.00",
                showInLegend: true,
                name: "Inteface price",
                dataPoints: api_price_dataPoints
            },
            {
                type: "line",
                xValueType: "dateTime",
                yValueFormatString: "$####.00",
                showInLegend: true,
                name: "Merged price",
                dataPoints: merged_price_dataPoints
            }
        ]
    });
    function toggleDataSeries(e: any) {
        if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
            e.dataSeries.visible = false;
        }
        else {
            e.dataSeries.visible = true;
        }
        chart.render();
    }

}
