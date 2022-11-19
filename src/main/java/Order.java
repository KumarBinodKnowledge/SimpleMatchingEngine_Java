package main.java;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Order implements Cloneable {
    static Map<String, Boolean> symbolStatusMap = new HashMap<>();
    String symbol;
    String side;
    String type;
    double price;
    double timeStamp;
    BufferedReader in = null;
    PrintWriter tradeWriter = null;

    // C'tor
    Order(String symbol, String side, String type, double price, double timeStamp) {
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.price = price;
        this.timeStamp = timeStamp;
    }

    // read the below file and populate symbol map
    /* symbols.csv
        AMZN,false
        AAPL,false
        GOOG,false
        TSLA,false
        FB,false
     */
    static void populateSymbolStatusMap() {
        // read symbols.txt and populate symbol map--------------------------------------------------------------
        try (BufferedReader in = new BufferedReader(new FileReader("src\\main\\resources\\symbols.csv"))) {
            String str = null;

            while ((str = in.readLine()) != null) {
                String[] tokens = str.split(",");
                String order_symbol = tokens[0];
                Boolean is_halted = Boolean.parseBoolean(tokens[1]);

                symbolStatusMap.put(order_symbol, is_halted);
                //  System.out.println("symbol Map = " + order_symbol + "   " + is_halted );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // getters
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String ordSymbol) {
        this.symbol = ordSymbol;
    }

    public double getPrice() {
        return price;
    }

    public String getSide() {
        return side;
    }

    // Setters
    public void setTime(double ordTime) {
        this.timeStamp = ordTime;
    }

    public void setOrderPrice(double ordPrice) {
        this.price = ordPrice;
    }

    public void setOrderSide(String ordSide) {
        this.side = ordSide;
    }

    public String toString() {
        return side + " , " + type + " , " + timeStamp + " , " + symbol + " , " + price;
    }

    // Validation return string --------------------------------------------------------------
    public ValidationString validateOrder() {
        if (!symbolStatusMap.containsKey(this.symbol)) {
            return ValidationString.SYMBOL_NOT_FOUND;
        } else if (symbolStatusMap.get(this.symbol) == true) {
            return ValidationString.SYMBOL_HALTED;
        } else if (!side.equals("buy") && !side.equals("sell")) {
            return ValidationString.INVALID_SIDE;
        }
        /*
        else if(!type.equals("limit") && !type.equals("market"))
        {
            return ValidationString.INVALID_TYPE;
        }
        */
        else if (type.equals("limit") && price <= 0) {
            return ValidationString.INVALID_PRICE;
        }
        return ValidationString.VALID;
    }

    public Boolean traderOrder(Order incomingOrder, OrderBooks orderBooks) {
        try {
            tradeWriter = new PrintWriter(new FileWriter("src\\main\\resources\\trades.txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("Type === " + incomingOrder.type);
        // limit order ------------------------------------------------------------------------------------------------
        if (incomingOrder.type.equals("limit")) {
            if (incomingOrder.side.equals("buy")) {
                Order topOfBook = orderBooks.sellOrderBook.peek();
                // if the sell order book exists
                if (topOfBook != null)   // there is an existing sell order book
                {
                    if (topOfBook.price <= incomingOrder.price)    // order is executable
                    {
                        orderBooks.sellOrderBook.remove(topOfBook);
                        tradeWriter.println("trades.txt : " + incomingOrder.symbol + " " + topOfBook.price + " "
                                + incomingOrder.timeStamp);
                       /* System.out.println("trades.txt : " + incomingOrder.symbol + " " + topOfBook.price + " "
                                + incomingOrder.timeStamp);*/
                        // TODO : change this to tradeTStamp and write SOP in the trades.txt
                        return true;
                    } else  // order not executable, place the order in the order book
                    {
                        orderBooks.buyOrderBook.add(incomingOrder);
                        /*System.out.println("order book addition : " + incomingOrder.symbol + " " + incomingOrder.side + " "
                                + incomingOrder.type +   " " + incomingOrder.price + " "
                                + incomingOrder.timeStamp);*/
                        return false;
                    }
                } else     // // there is not an existing sell order book
                {
                    orderBooks.buyOrderBook.add(incomingOrder);  // Will rank and place at right place
                   /* System.out.println("order book addition : " + incomingOrder.symbol + " " + incomingOrder.side + " "
                            + incomingOrder.type +   " " + incomingOrder.price + " "
                            + incomingOrder.timeStamp);*/
                    return false;
                }
            } else if (incomingOrder.side.equals("sell"))  // mirror image of buy logic
            {
                Order topOfBook = orderBooks.buyOrderBook.peek();
                // if the buy_order_book exists------------------
                if (topOfBook != null) {
                    if (topOfBook.price >= incomingOrder.price) {
                        orderBooks.buyOrderBook.remove(topOfBook);
                        tradeWriter.println("trades.txt : " + incomingOrder.symbol + " " + topOfBook.price + " "
                                + incomingOrder.timeStamp);
                       /* System.out.println("trades.txt : " + incomingOrder.symbol + " " + topOfBook.price + " "
                                + incomingOrder.timeStamp);*/
                        // TODO : change this to tradeTStamp and write SOP in the trades.txt
                        return true;
                    } else {
                        orderBooks.sellOrderBook.add(incomingOrder);  // add in the order book
                       /* System.out.println("order book addition : " + incomingOrder.symbol + " " + incomingOrder.side + " "
                                + incomingOrder.type +   " " + incomingOrder.price + " "
                                + incomingOrder.timeStamp);*/
                        return false;
                    }
                }
                // There is no existing sell orderbook
                else {
                    orderBooks.sellOrderBook.add(incomingOrder);  // Will rank and place at right place
                   /* System.out.println("order book addition : " + incomingOrder.symbol + " " + incomingOrder.side + " "
                            + incomingOrder.type +   " " + incomingOrder.price + " "
                            + incomingOrder.timeStamp);*/
                    return false;
                }
            }
        }  // --- limit order logic ends -----

        // ------ market order logic starts ---------------------------------------------------------------------------
        else if (incomingOrder.type.equals("market"))  // market orders doesn't add into order books
        {
            if (incomingOrder.side.equals("buy")) {
                Order topOfBook = orderBooks.sellOrderBook.peek();
                if (topOfBook != null) {
                    orderBooks.sellOrderBook.remove(topOfBook);
                    tradeWriter.println("trades.txt : " + incomingOrder.symbol + " " + topOfBook.price + " "
                            + incomingOrder.timeStamp);
                   /* System.out.println("trades.txt : " + incomingOrder.symbol + " " + topOfBook.price + " "
                            + incomingOrder.timeStamp);*/
                    // TODO : change this to tradeTStamp and write SOP in the trades.txt
                    return true;
                } else {
                    // reject the order with reason : "no market for the symbol " + order.symbol
                    String rejectReason = "No market for the symbol " + incomingOrder.symbol;
                    SimpleMatchingEngine.writer.println("rejected.txt : "  + rejectReason + " \nRejected Order "
                            + incomingOrder.symbol + " " + incomingOrder.side + " "
                            + incomingOrder.type +   " " + incomingOrder.price + " "
                            + incomingOrder.timeStamp);
                    /*System.out.println("rejected.txt : "  + rejectReason + " \nRejected Order "
                            + incomingOrder.symbol + " " + incomingOrder.side + " "
                            + incomingOrder.type +   " " + incomingOrder.price + " "
                            + incomingOrder.timeStamp);*/
                    // TODO : write SOP to the rejected.txt
                    return false;
                }
            } else if (incomingOrder.side.equals("sell")) {
                Order topOfBook = orderBooks.buyOrderBook.peek();
                if (topOfBook != null) {
                    orderBooks.buyOrderBook.remove(topOfBook);
                    tradeWriter.println("trades.txt : " + incomingOrder.symbol + " " + topOfBook.price + " "
                            + incomingOrder.timeStamp);
                    /*System.out.println("trades.txt : " + incomingOrder.symbol + " " + topOfBook.price + " "
                            + incomingOrder.timeStamp);*/
                    // TODO : change this to tradeTStamp and write SOP in the trades.txt
                    return true;
                } else {
                    String rejectReason = "No market for the symbol " + incomingOrder.symbol;
                    SimpleMatchingEngine.writer.println("rejected.txt : "  + rejectReason + " \nRejected Order "
                            + incomingOrder.symbol + " " + incomingOrder.side + " "
                            + incomingOrder.type +   " " + incomingOrder.price + " "
                            + incomingOrder.timeStamp);
                   /* System.out.println("rejected.txt : "  + rejectReason + " \nRejected Order "
                            + incomingOrder.symbol + " " + incomingOrder.side + " "
                            + incomingOrder.type +   " " + incomingOrder.price + " "
                            + incomingOrder.timeStamp);*/
                    // TODO : write SOP to the rejected.txt
                    return false;
                }
            }
        } // market order logic ends ---------------------------------------------------------------------------------
        return false;
    } // tradeOrder
}