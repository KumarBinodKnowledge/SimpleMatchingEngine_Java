package main.java;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SimpleMatchingEngine {
    // symbol to corresponding buy_book and sell_book map.
    // Per Symbol there should be 2 OrderBooks (buy_book and sell_book)
    static Map<String, OrderBooks> symbol2OrderBookMap;
    static PrintWriter writer = null;
    static PrintWriter buyOrderWriter = null;
    static PrintWriter sellOrderWriter = null;

    public static void main(String args[]) {
        symbol2OrderBookMap = new HashMap<String, OrderBooks>();
        // Todo : Improve the design by having 2 maps instead of one.
        // 1. symbol2BuyOrderBookMap
        // 2. symbol2SellOrderBokMap

        try
        {
            // the '/' and '\\' both path separator works.
            writer = new PrintWriter(new BufferedWriter(new FileWriter("src/main/resources/output/rejected.txt", true)));
            buyOrderWriter = new PrintWriter(new BufferedWriter(new FileWriter("src/main/resources/output/buy_orderbook.txt", true)));
            sellOrderWriter = new PrintWriter(new BufferedWriter(new FileWriter("src/main/resources/output/sell_orderbook.txt", true)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Order.populateSymbolStatusMap();
        // Read the orders.csv
        try (BufferedReader in = new BufferedReader(new FileReader("src/main/resources/input/orders.csv"))) {
            String strOrder;
            while ((strOrder = in.readLine()) != null) {
                String[] tokens = strOrder.split(",");
                String orderSymbol = tokens[0];
                String orderSide = tokens[1];
                String orderType = tokens[2];
                double orderPrice = -1;
                if (tokens[3].trim().length() != 0) {
                    orderPrice = Double.parseDouble(tokens[3]);
                }
                double order_timeStamp = Double.parseDouble(tokens[4]); // convert this to double

                Order order = new Order(orderSymbol, orderSide, orderType, orderPrice, order_timeStamp);

                ValidationString validationString = order.validateOrder();
                if (validationString != ValidationString.VALID) {
                    // System.out.println("Order Invalid : " + validationString  + " " + order.symbol);
                    writer.println("rejected.txt : " + validationString + " " + order.symbol + " " +
                            "rejected order : " + order.symbol + " " + order.side + " "
                            + order.type + " " + order.price + " "
                            + order.timeStamp);


                    /*System.out.println("rejected.txt : " + validationString + " "  + order.symbol + " " +
                                       "rejected order : " + order.symbol +   " "  + order.side   + " "
                                                           + order.type +     " "  + order.price  + " "
                                                           + order.timeStamp);*/

                    continue;  // stop processing the order further, read next incoming order.
                }
                if (symbol2OrderBookMap.containsKey(order.symbol)) {
                    order.traderOrder(order, symbol2OrderBookMap.get(order.symbol));
                } else  // if order book ( buy_book or sell_book ) is empty
                {
                    // Create a limit order book very 1st time for this symbol
                    // Limit order
                    OrderBooks orderBooks = new OrderBooks();
                    symbol2OrderBookMap.put(order.symbol, orderBooks);
                    if (order.side.equals("buy")) {
                        orderBooks.buyOrderBook.add(order);
                    } else if (order.side.equals("sell")) {
                        orderBooks.sellOrderBook.add(order);
                    }
                    // Market order
                    // Reject the incoming market order with reason : "No market for the symbol " + order.symbol
                    if (order.type.equals("market")) {

                        // add an entry in the reject.txt
                        String rejectReason = null;
                        rejectReason = "No market for the symbol " + order.symbol;
                        writer.println("rejected.txt : " + rejectReason + " " +
                                "rejected order : " + order.symbol + " " + order.side + " "
                                + order.type + " " + order.price + " "
                                + order.timeStamp);



                       /* System.out.println("rejected.txt : " + rejectReason + " " +
                                            "rejected order : " + order.symbol + " " + order.side + " "
                                                                + order.type + " " + order.price + " "
                                                                + order.timeStamp);*/
                        continue;

                    }
                }

            } // end of reading input order file
            System.out.println("rejected data written in rejected.txt file");
            // At EoD cancel all orders from the order books and dump leftover orderbooks' into the OrderBookStatusEOD.txt --------------------------------------------------------
            for (OrderBooks orderBooksItem : symbol2OrderBookMap.values()) {
                // Dump the Orders from buy_order book in buy_order.txt
                while (!orderBooksItem.buyOrderBook.isEmpty()) {
                    Order order = orderBooksItem.buyOrderBook.remove();
                    buyOrderWriter.println("buy_orderbook.txt---------- : "  + order.symbol + " " + order.side +
                            " " + order.type   + " " + order.price +
                            " " +order.timeStamp);
                    /*System.out.println("buy_orderbook.txt---------- : "  + order.symbol + " " + order.side +
                                                                     " " + order.type   + " " + order.price +
                                                                     " " +order.timeStamp);*/
                    // TODO Remove the SOP, write order in the OrderBookStatusEOD.txt
                }
                // Dump Orders from sell_order book in sell_order.txt -
                while (!orderBooksItem.sellOrderBook.isEmpty()) {
                    Order order = orderBooksItem.sellOrderBook.remove();
                    sellOrderWriter.println("sell_orderbook.txt         : "    + order.symbol + " " + order.side +
                            " " + order.type +   " " + order.price +
                            " " + order.timeStamp);
                    /*System.out.println("sell_orderbook.txt         : "    + order.symbol + " " + order.side +
                                                                     " " + order.type +   " " + order.price +
                                                                     " " + order.timeStamp);*/
                    // TODO remove the SOP and write order in the OrderBookStatusEOD.txt
                }
            }
            //----------------------------------------------------------------------------------------------------
        } catch (IOException e) {
            System.out.println("File Read Error");
        }
        // TODO : Handle more exceptions
    }
}