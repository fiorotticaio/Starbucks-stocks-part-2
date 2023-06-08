import styles from "../styles/components/HeaderButton.module.css";

interface HeaderButtonProps {
  text: string;
  active?: boolean;
  onClick?: () => void;
}

export default function HeaderButton({
  text,
  active = false,
  onClick,
}: HeaderButtonProps) {
  return (
    <button
      className={active ? styles.activeBtn : styles.Btn}
      onClick={onClick}
    >
      {text.toUpperCase()}
    </button>
  );
}
