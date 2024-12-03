package org.K1ll4a;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileFilterUtility {
    public static void main(String[] args) {
        // Проверка аргументов
        if (args.length == 0) {
            System.err.println("Не указаны входные файлы.");
            return;
        }

        // Параметры
        boolean appendMode = Arrays.asList(args).contains("-a");
        boolean shortStats = Arrays.asList(args).contains("-s");
        boolean fullStats = Arrays.asList(args).contains("-f");
        String outputPath = "./";
        String prefix = "";

        // Парсинг опций
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o") && i + 1 < args.length) {
                outputPath = args[++i];
            } else if (args[i].equals("-p") && i + 1 < args.length) {
                prefix = args[++i];
            }
        }

        // Файлы для вывода
        Map<String, List<String>> data = new HashMap<>();
        data.put("integers", new ArrayList<>());
        data.put("floats", new ArrayList<>());
        data.put("strings", new ArrayList<>());

        // Чтение входных файлов
        for (String fileName : args) {
            if (fileName.startsWith("-")) continue; // Пропуск опций

            try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    classifyData(line.trim(), data);
                }
            } catch (IOException e) {
                System.out.println();
            }
        }

        // Запись данных в файлы
        writeDataToFile(outputPath, prefix, "integers", data.get("integers"), appendMode);
        writeDataToFile(outputPath, prefix, "floats", data.get("floats"), appendMode);
        writeDataToFile(outputPath, prefix, "strings", data.get("strings"), appendMode);

        // Вывод статистики
        if (shortStats || fullStats) {
            printStatistics(data, shortStats, fullStats);
        }
    }

    private static void classifyData(String line, Map<String, List<String>> data) {
        try {
            Integer.parseInt(line);
            data.get("integers").add(line);
        } catch (NumberFormatException e1) {
            try {
                Double.parseDouble(line);
                data.get("floats").add(line);
            } catch (NumberFormatException e2) {
                data.get("strings").add(line);
            }
        }
    }

    private static void writeDataToFile(String outputPath, String prefix, String type, List<String> data, boolean append) {
        if (data.isEmpty()) return;

        Path filePath = Paths.get(outputPath, prefix + type + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, append ? StandardOpenOption.APPEND : StandardOpenOption.CREATE)) {
            for (String line : data) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Ошибка записи в файл " + filePath + ": " + e.getMessage());
        }
    }

    private static void printStatistics(Map<String, List<String>> data, boolean shortStats, boolean fullStats) {
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            String type = entry.getKey();
            List<String> values = entry.getValue();

            System.out.println("Статистика для " + type + ":");
            if (values.isEmpty()) {
                System.out.println("Нет данных.");
                continue;
            }

            if (shortStats) {
                System.out.println("Количество: " + values.size());
            }

            if (fullStats) {
                if (type.equals("integers") || type.equals("floats")) {
                    List<Double> numericValues = values.stream().map(Double::parseDouble).collect(Collectors.toList());
                    System.out.println("Минимум: " + Collections.min(numericValues));
                    System.out.println("Максимум: " + Collections.max(numericValues));
                    System.out.println("Сумма: " + numericValues.stream().mapToDouble(Double::doubleValue).sum());
                    System.out.println("Среднее: " + numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0));
                } else {
                    List<Integer> lengths = values.stream().map(String::length).collect(Collectors.toList());
                    System.out.println("Минимальная длина: " + Collections.min(lengths));
                    System.out.println("Максимальная длина: " + Collections.max(lengths));
                }
            }
        }
    }
}
