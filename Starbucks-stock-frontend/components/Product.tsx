import Image, { StaticImageData } from "next/image";

import styles from "../styles/components/Product.module.css";
import api from "@/services/api";
import { useState } from "react";


interface ProductProps {
  name: string;
  size: string;
  image: StaticImageData;
  price: number;
}

export default function Product({ name, size, image, price }: ProductProps) {

  const [ buttonClicked, setButtonClicked ] = useState<boolean>(false);
  
  async function handleClick() {
    setButtonClicked(true)
    await api.get(`/buy/${price.toString()}`)
    setTimeout(()=>setButtonClicked(false),50)
  }

  return (
    <div className={styles.productContainer}>
      <Image src={image} alt={name} />
      <h4>
        {name} - {size}
      </h4>
      <h2>$ {price.toFixed(2)}</h2>
      <button 
        className={styles.buyButton}
        id={buttonClicked?styles.clicked:''}
        onClick={handleClick}>BUY</button>
    </div>
  );
}
