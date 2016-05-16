/*
 * Copyright 2016 Valery Butuzov
 * <valery.butuzov@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */
class Result {
    int lineNumber
    String line
    ArrayList noAscii
    String numberSymbol
    String message


    Result(int lineNumber, String line, ArrayList noAscii, String message) {
        this.lineNumber = lineNumber
        this.line = line
        this.noAscii = noAscii
        this.numberSymbol = ""
        this.message = message
    }

    def String toString(){
        return "In line №" + this.lineNumber + this.message + "\n" +
                this.line +  "\n" +
                symbol()
    }

    String symbol(){
        for(int i = 0; i < this.noAscii.size(); i++){
            while(this.numberSymbol.size() != this.noAscii.get(i)){
                this.numberSymbol += " "
            }
            this.numberSymbol += "^"
        }

        return  this.numberSymbol
    }
}
