import React from 'react';
import { Line } from 'react-chartjs-2';
import { LineElement, Chart, CategoryScale, LinearScale, PointElement, Legend } from "chart.js";
Chart.register(LineElement);
Chart.register(CategoryScale);
Chart.register(LinearScale);
Chart.register(PointElement);
Chart.register(Legend)

var dataAPI = [4]
var dataWEB = [4]
var dataMER = [4]
var labels = [""]

const THRESHOLD = 15

export const sendToGraph = (api: number, web: number, merged: number) => {
  
  if (dataAPI.length>=THRESHOLD) dataAPI.splice(0, 1)
  else labels.push("")

  if (dataWEB.length>=THRESHOLD) dataWEB.splice(0, 1)
  if (dataMER.length>=THRESHOLD) dataMER.splice(0, 1)
  
  dataAPI.push(api)
  dataWEB.push(web)
  dataMER.push(merged)
}

const updateState = () => {
  return {
    labels,
    datasets: [
      { label: 'MERGED Price', data: dataMER, borderColor: "#3ee7a1" },
      { label: 'API Price', data: dataAPI, borderColor: "#007042" },
      { label: 'WEB Price', data: dataWEB, borderColor: "#04462b" },
    ]
  }
}

export default class DynamicDoughnut extends React.Component{
  
  componentDidMount(): void {
		setInterval(() => {
      this.setState(updateState());
		}, 3000);
  }

  render() {
    return (
      <div style={{height:"30rem"}}>
        <h2>Prices history</h2>
        {
          this.state && (
            <Line
              data={this.state as any}
              options={{
                plugins: {
                  legend: { position: 'top' as const },
                },
              }}
              width={250}
              height={100}
            />
          )
        }
        
      </div>
    );
  }
};