package org.query.calc;

import it.unimi.dsi.fastutil.doubles.Double2DoubleSortedMap;
import org.query.calc.model.Record;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Map;

public class Aggregator {
    private final Double2DoubleSortedMap joinedData;
    private final Map<Double, Double> newData;
    private final LinkedList<Record> resultList;
    private final int limit;

    public Aggregator(Map<Double, Double> newData, Double2DoubleSortedMap joinedData, int limit) {
        this.joinedData = joinedData;
        this.newData = newData;
        this.limit = limit;
        this.resultList = new LinkedList<>();
    }

    public void calcAndSaveResult(Path output)  throws IOException {
        newData.entrySet().forEach(record -> {
            boolean notFound = true;
            for (Double d : joinedData.tailMap(record.getKey().doubleValue()).keySet()) {
                if (record.getKey() < d) {
                    addRecord(record.getKey(), record.getValue() * joinedData.get(d.doubleValue()));
                    notFound = false;
                    break;
                }
            }
            if (notFound) {
                addRecord(record.getKey(), 0.0);
            }
        });
        writeResult(output);
    }

    private void addRecord(double label, double result) {

        int i;
        for (i = 0; i < resultList.size() && resultList.get(i).getCol2() >= result; i++) ;
        if (i < resultList.size() || i < limit) {
            resultList.add(i, new Record(label, result));
            if (resultList.size() > limit) {
                resultList.removeLast();
            }
        }
    }

    private void writeResult(Path output) throws IOException {
        try(BufferedWriter bwr = Files.newBufferedWriter(output)) {
            bwr.write(String.valueOf(resultList.size()));
            for (Record r : resultList) {
                bwr.newLine();
                bwr.write(r.toString());
            }

        }
    }
}
