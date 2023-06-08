import Image from "next/image";
import { useState } from "react";

import HeaderButton from "./HeaderButton";

import logo from "../public/LogoSTBX.svg";
import texto from "../public/TextoSTBX.svg";

import styles from "../styles/components/Header.module.css";

interface ButtonStateProps {
  shop: boolean;
  info: boolean;
}

export default function Header() {
  const [buttonsState, setButtonsState] = useState<ButtonStateProps>({
    shop: true,
    info: false,
  });

  function handleShopClick() {
    if (buttonsState.shop) return;
    setButtonsState({
      shop: true,
      info: false,
    });
  }

  function handleInfoClick() {
    if (buttonsState.info) return;
    setButtonsState({
      shop: false,
      info: true,
    });
  }

  return (
    <header className={styles.headerContainer}>
      <section className={styles.logo}>
        <Image src={logo} alt="Starbux Logo" className={styles.logoImg} />
        <section className={styles.logoText}>
          <Image src={texto} alt="Starbux" />
          <h4>STOCKS SIMULATOR</h4>
        </section>
      </section>
      <section className={styles.buttons}>
        <HeaderButton
          text="Shop"
          onClick={handleShopClick}
          active={buttonsState.shop}
        />
        <HeaderButton
          text="Info"
          onClick={handleInfoClick}
          active={buttonsState.info}
        />
      </section>
    </header>
  );
}
