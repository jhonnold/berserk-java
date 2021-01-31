package me.honnold.bitboard;

import java.util.Collection;

public class Berserk {
    public static void main(String[] args) {
        Position p = new Position("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/1R2K2R b Kkq - 1 1");

        System.out.println(p);
        Iterable<Move> moves = p.getMoves();
        System.out.println(((Collection<?>) moves).size());
        for (Move m : moves) {
            System.out.println(m);
        }
    }
}
