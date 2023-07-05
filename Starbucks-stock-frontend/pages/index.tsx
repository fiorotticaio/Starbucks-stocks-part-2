import styles from "@/styles/pages/Home.module.css";

import Header from "@/components/Header";
import Product from "@/components/Product";

// import Graph from "@/components/Graph";
// import dynamic from 'next/dynamic'
// const Graph = dynamic(() => import("@/components/Graph"))
import Graph, { sendToGraph } from "@/components/Graph"

import mediumCupImg from "@/public/mediumCup.png";
import largeCupImg from "@/public/largeCup.png";
import { useEffect, useState } from "react";
import api from "@/services/api";
import HistoryTable from "@/components/HistoryTable";
import Head from "next/head";

interface HistoricProps {
  id: string;
  value: string;
}

export default function Home() {

  const [ coffee_price, setCoffePrice ] = useState<number>(0);
  const [ web_coffee_price, setWebPrice ] = useState<number>(0);
  const [ api_coffee_price, setApiPrice ] = useState<number>(0);
  const [ coffee_price_history, setCoffeePriceHistory ] = useState<number[]>([]);
  const [ web_coffee_price_history, setWebPriceHistory ] = useState<number[]>([]);
  const [ api_coffee_price_history, setApiPriceHistory ] = useState<number[]>([]);
  const [ amount, setAmount ] = useState<number[]>([]);
  const [ date, setDate ] = useState<Date[]>([]);
  const [ id, setId ] = useState<string[]>([]);
  
  async function fetchKafka() {
    const response = await api.get('/all')
    const history = await api.get('/historic')
    
    if (response.data) {
      setCoffePrice(parseFloat(response.data.coffee_price))
      setWebPrice(parseFloat(response.data.web_coffee_price))
      setApiPrice(parseFloat(response.data.api_coffee_price))
      setCoffeePriceHistory([...coffee_price_history, parseFloat(response.data.coffee_price)])
      setWebPriceHistory([...web_coffee_price_history, parseFloat(response.data.web_coffee_price)])
      setApiPriceHistory([...api_coffee_price_history, parseFloat(response.data.api_coffee_price)])
      sendToGraph(response.data.api_coffee_price, response.data.web_coffee_price, response.data.coffee_price)
    }

    if (history.data) {
      const historic = history.data.historic as HistoricProps[]
      const id = historic.map((item, index)=>item.id)
      setId(id)

      const amount = historic.map((item, index)=>{
        const a = parseFloat(item.value.split(' ')[1])
        return a
      })
      const date = historic.map((item, index)=>{
        const date = new Date(item.value.split(' ')[3])
        const formattedDate = new Intl.DateTimeFormat("pt-BR", {
          year: "numeric",
          month: "2-digit",
          day: "2-digit",
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
        }).format(date);
        return date
      })
      setAmount(amount)
      setDate(date)
    };
  }

  useEffect(()=>{
    setInterval(fetchKafka, 1000)
  },[])

  return (
    <div className={styles.home}>
      <Head>
        <title>Starbucks Kafka</title>
      </Head>
      <Header />
      <div className={styles.banner} />
      <div className={styles.content}>
        <section className={styles.contentTitle}>
          <h1 onClick={fetchKafka}>SHOP</h1>
          <h5>
            From the beginning, starbucks set out to be a different kind of
            company. One that not only celebrated coffee but also connection.
            We&apos;re a neighborhood gathering place, a part of your daily
            routine. Get to know us and you&apos;ll see: we are so much more
            than what we brew.
          </h5>
          <table className={styles.infoTable}>
            <thead>
              <tr>
                <th>Description</th>  
                <th>Value</th>  
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>REAL STOCK API PRICE</td>
                <td>{api_coffee_price?api_coffee_price.toFixed(2):0}</td>
              </tr>
              <tr>
                <td>USER INTERFACE PRICE</td>
                <td>{web_coffee_price?web_coffee_price.toFixed(2):0}</td>
              </tr>
              <tr>
                <td>MERGED PRICE</td>
                <td>{coffee_price?coffee_price.toFixed(2):0}</td>
              </tr>
            </tbody>
          </table>
        </section>
        <section className={styles.contentProducts}>
          <Product
            image={mediumCupImg}
            price={coffee_price}
            name="Medium Cup"
            size="250ml"
            productKey="sm_coffee"
          />
          <Product
            image={largeCupImg}
            price={coffee_price*1.5}
            name="Large Cup"
            size="400ml"
            productKey="lg_coffee"
          />
          <section className={styles.contentHistory}>
            <HistoryTable amount={amount} date={date} id={id}/>
          </section>
        </section>
        <section className={styles.contentGraph}>
          <Graph />
        </section>
      </div>
      <div style={{marginTop: "10vh", marginBottom: "-50vh"}} className={styles.banner} />
    </div>
  );
}
