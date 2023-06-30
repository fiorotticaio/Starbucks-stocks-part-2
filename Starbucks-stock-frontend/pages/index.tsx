import styles from "@/styles/pages/Home.module.css";

import Header from "@/components/Header";
import Product from "@/components/Product";

import mediumCupImg from "@/public/mediumCup.png";
import largeCupImg from "@/public/largeCup.png";
import { useEffect, useState } from "react";
import api from "@/services/api";

export default function Home() {

  const [ coffee_price, setCoffePrice ] = useState<number>(0);
  const [ web_coffee_price, setWebPrice ] = useState<number>(0);
  const [ api_coffee_price, setApiPrice ] = useState<number>(0);

  async function fetchKafka() {
    const response = await api.get('/all')
    
    
    if (response.data) {
      setCoffePrice(parseFloat(response.data.coffee_price))
      setWebPrice(parseFloat(response.data.web_coffee_price))
      setApiPrice(parseFloat(response.data.api_coffee_price))
    }
  }

  useEffect(()=>{
    setInterval(fetchKafka, 1000)
  },[])

  return (
    <div className={styles.home}>
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
          />
          <Product
            image={largeCupImg}
            price={coffee_price*1.5}
            name="Large Cup"
            size="400ml"
          />
        </section>
      </div>
      <div style={{marginTop: "10vh", marginBottom: "-50vh"}} className={styles.banner} />
    </div>
  );
}
