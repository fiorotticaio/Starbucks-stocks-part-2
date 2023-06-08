import styles from "@/styles/pages/Home.module.css";

import Header from "@/components/Header";
import Product from "@/components/Product";

import mediumCupImg from "@/public/mediumCup.png";
import largeCupImg from "@/public/largeCup.png";
import { useEffect, useState } from "react";
import api from "@/services/api";

export default function Home() {

  const [ coffee_price_0, setcfp0 ] = useState<number>(0);
  const [ coffee_price_1, setcfp1 ] = useState<number>(0);
  const [ coffee_price_2, setcfp2 ] = useState<number>(0);

  async function fetchKafka() {
    const response = await api.get('/all')
    
    
    if (response.data) {
      setcfp0(parseFloat(response.data.coffee_price_0))
      setcfp1(parseFloat(response.data.coffee_price_1))
      setcfp2(parseFloat(response.data.coffee_price_2))
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
                <td>{coffee_price_0?coffee_price_0.toFixed(2):0}</td>
              </tr>
              <tr>
                <td>USER INTERFACE PRICE</td>
                <td>{coffee_price_1?coffee_price_1.toFixed(2):0}</td>
              </tr>
              <tr>
                <td>MERGED PRICE</td>
                <td>{coffee_price_2?coffee_price_2.toFixed(2):0}</td>
              </tr>
            </tbody>
          </table>
        </section>
        <section className={styles.contentProducts}>
          <Product
            image={mediumCupImg}
            price={coffee_price_2}
            name="Medium Cup"
            size="250ml"
          />
          <Product
            image={largeCupImg}
            price={coffee_price_2*1.5}
            name="Large Cup"
            size="400ml"
          />
        </section>
      </div>
      <div style={{marginTop: "10vh", marginBottom: "-50vh"}} className={styles.banner} />
    </div>
  );
}
