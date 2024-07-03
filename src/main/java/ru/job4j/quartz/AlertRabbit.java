package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    public static void main(String[] args) throws ClassNotFoundException {
        Properties config = getProperties();
        Class.forName(config.getProperty("rabbit.driver"));
        String url = config.getProperty("rabbit.url");
        String user = config.getProperty("rabbit.username");
        String password = config.getProperty("rabbit.password");
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            List<Long> store = new ArrayList<>();
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("store", store);
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            int interval = Integer.parseInt(config.getProperty("rabbit.interval"));
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
            System.out.println(store);
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream input = AlertRabbit.class.getClassLoader().
                getResourceAsStream("rabbit.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        public void add(Connection connection, LocalDateTime dateTime) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO rabbit(created_date) VALUES (?)")) {
                statement.setTimestamp(1, Timestamp.valueOf(dateTime));
                statement.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            List<Long> store = (List<Long>) context.getJobDetail().getJobDataMap().get("store");
            store.add(System.currentTimeMillis());
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            add(connection, LocalDateTime.now());
        }
    }
}