package com.ef;

import com.ef.dao.AccessLogDao;
import com.ef.model.AccessLog;
import com.ef.model.IPLock;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class Parser {

    private static final String argStartDate = "--startDate";
    private static final String argDuration = "--duration";
    private static final String argThreshold = "--threshold";
    private static final String argAccesslog = "--accesslog";
    private static final Logger LOG = Logger.getLogger(Parser.class);

    public static void main(final String[] args) throws Exception {

        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ERROR);

        Date startDate = null;
        String duration = null;
        Integer threshold = null;
        FileReader file = null;

        for (String arg : args) {
            if (arg.contains("--") && arg.contains("=")){
                String var[] = arg.split("=");

                switch (var[0]) {
                    case argStartDate:
                        try {
                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
                            startDate = format.parse(var[1]);
                        } catch (ParseException e) {
                            LOG.error("Start Date: invalid format, the format is yyyy-MM-dd.HH:mm:ss");
                            e.printStackTrace();
                        }
                        break;
                    case argDuration:
                        if (var[1].equals(Duration.hourly.name())){
                            duration = Duration.hourly.name();
                        } else if (var[1].equals(Duration.daily.name())){
                            duration = Duration.daily.name();
                        } else {
                            LOG.error("variable duration must be " + Duration.hourly.name() + " or " + Duration.daily.name());
                        }
                        break;
                    case argThreshold:
                        try {
                            threshold = Integer.parseInt(var[1]);
                        } catch (NumberFormatException e) {
                            LOG.error("threshold must be a integer");
                            e.printStackTrace();
                        }
                        break;
                    case argAccesslog:
                        try {
                            file = new FileReader(var[1]);
                        } catch (Exception e) {
                            LOG.error("File not found");
                            e.printStackTrace();
                        }
                }
            }
        }

        if (startDate != null && duration != null && threshold != null && file != null){
            AccessLogDao dao = new AccessLogDao();
            BufferedReader buffer = new BufferedReader(file);
            String line = buffer.readLine();

            Date finalDate;
            if (duration.equals(Duration.hourly.name())) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(startDate.getTime());
                calendar.add(Calendar.HOUR, 1);
                finalDate = calendar.getTime();
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(startDate.getTime());
                calendar.add(Calendar.HOUR, 24);
                finalDate = calendar.getTime();
            }

            System.out.println("Loading file...");
            while (line != null) {
                String var[] = line.split("\\|");
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Date linhaDate = format.parse(var[0]);

                AccessLog log = new AccessLog();
                log.setAccessDate(linhaDate);
                log.setIp(var[1]);
                log.setRequest(var[2]);
                log.setStatus(Integer.valueOf(var[3]));
                log.setUserAgent(var[4]);

                dao.save(log);

                line = buffer.readLine();
            }
            System.out.println("file loaded... \n");

            Collection<AccessLog> logs = dao.findByParameters(startDate, finalDate, threshold);

            if (logs != null) {
                System.out.println("ips found:");
                Collection<String> ips = new ArrayList<>();
                for (AccessLog log : logs) {
                    System.out.println(log.getIp());
                    ips.add(log.getIp());
                }

                Collection<AccessLog> logsLock;
                if (duration.equals(Duration.hourly.name())) {
                    logsLock = dao.findLocks(ips, startDate, finalDate, Duration.hourly.getLimit());
                } else {
                    logsLock = dao.findLocks(ips, startDate, finalDate, Duration.daily.getLimit());
                }

                for (AccessLog log : logs){
                    IPLock lock = new IPLock();
                    lock.setIp(log.getIp());
                    if (logsLock != null && !logsLock.isEmpty() && logsLock.contains(log)){
                        if (duration.equals(Duration.hourly.name())) {
                            lock.setComment("More than " + Duration.hourly.getLimit() + " requests per hour.");
                        } else {
                            lock.setComment("More than " + Duration.daily.getLimit() + " requests per day.");
                        }
                    } else {
                        lock.setComment("Not Blocked");
                    }
                    dao.save(lock);
                }

            } else {
                System.out.println("no data found with the reported parameters");
            }

        }

    }

    private enum Duration{
        hourly(200), daily(500);

        private Integer limit;

        Duration(Integer limit) {
            this.limit = limit;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }
    }
}