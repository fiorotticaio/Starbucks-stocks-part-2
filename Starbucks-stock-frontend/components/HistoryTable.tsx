import styles from '../styles/components/HistoryTable.module.css'

interface HistoryTableProps {
    id: string[];
    amount: number[];
    date: Date[];
}

export default function HistoryTable({ id, amount, date }: HistoryTableProps) {
    return (
        <div className={styles.historyTable}>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th></th>
                        <th>Total Amount</th>
                        <th></th>
                        <th>Date</th>
                    </tr>
                </thead>
                <tbody>
                    {id.map((item, index) => (
                        <tr key={index}>
                            <td>{item}</td>
                            <td></td>
                            <td>{amount[index]}</td>
                            <td></td>
                            <td>{date[index].toUTCString()}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    )
}