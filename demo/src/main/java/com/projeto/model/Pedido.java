package com.projeto.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pedido {

    public enum Status{
        CONCLUIDO,PREPARANDO,CANCELADO
    }

    private Map<Item,Integer> mapa =  new HashMap<>();

    private int id;

    public Map<Item, Integer> getMapa() {
        return mapa;
    }

    public void setMapa(Map<Item, Integer> mapa) {
        this.mapa = mapa;
    }

    private double valorTotal;

    private Status status;

    private int idComanda;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getIdComanda() {
        return idComanda;
    }

    public void setIdComanda(int idComanda) {
        this.idComanda = idComanda;
    }

}
