import CanvasJSReact from '@canvasjs/react-charts';


interface GraphProps {
    dataX: string[];
    dataY: number[];
}

export default function Graph({ dataX, dataY }: GraphProps}) {
    let CanvasJS = CanvasJSReact.CanvasJS;
    let CanvasJSChart = CanvasJSReact.CanvasJSChart;
    let updateInterval = 1000;
    const componentDidMount = () => {
        setInterval(() => { updateChart() }, updateInterval);
    }
    const updateChart = () => {
        let dpsX: string[] = dataX;
        let dpsY: number[] = dataY;
        if (dpsX.length >   20) {
            dpsX.shift();
            dpsY.shift();
        }
        chart.render();
    }
    const chart = new CanvasJSChart("chartContainer", {
        title: {
            text: "Live Data"
        },
        data: [{
            type: "line",
            dataPoints:
                dataX.map((x, index) => {
                    return { x: new Date(x), y: dataY[index] }
                })
        }]
    });
    componentDidMount();

    return (
        <div>
            <div id="chartContainer" style={{ height: 300, width: 600 }}></div>
        </div>
    );
}