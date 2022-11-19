package main.java;

import java.util.Comparator;
import java.util.PriorityQueue;

// This class represents 2 order books per symbol buy_book and sell_book-------------------------------
public class OrderBooks
{
    PriorityQueue<Order> buyOrderBook;
    PriorityQueue<Order> sellOrderBook;

    OrderBooks()
    {
        // buy order book as price time priority queue
        buyOrderBook = new PriorityQueue<Order>(new Comparator<Order>()
        {
            public int compare(Order x, Order y)
            {
                if (x.price < y.price)
                {
                    return 1;
                }
                else if (x.price > y.price)
                {
                    return -1;
                }
                else
                {
                    if (x.timeStamp < y.timeStamp)
                    {
                        return 1;
                    }
                    else
                    {
                        return -1;
                    }
                }
            }
        });

        // sell order book as price time priority queue
        sellOrderBook = new PriorityQueue<Order>(new Comparator<Order>()
        {
            public int compare(Order x, Order y)
            {
                if (x.price < y.price)
                {
                    return 1;
                }
                else if (x.price > y.price)
                {
                    return -1;
                }
                else
                {
                    if (x.timeStamp < y.timeStamp)
                    {
                        return -1;
                    }
                    else
                    {
                        return 1;
                    }
                }
            }
        });
    }
}