package com.ef.dao;

import com.ef.model.AccessLog;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unchecked")
public class AccessLogDao extends AbstractDao {

    public Collection<AccessLog> findByParameters(Date initialDate, Date finalDate, Integer threshold){
        Transaction tx = null;
        List<AccessLog> logs = null;
        try {
            tx = getSession().beginTransaction();

            Query query = getSession().createNativeQuery("SELECT * " +
                    "FROM access_log al  " +
                    "WHERE al.access_date BETWEEN :initialData AND :finalDate " +
                    "GROUP BY al.ip HAVING count(al.ip) >= :threshold ", AccessLog.class);

            query.setParameter("initialData", initialDate);
            query.setParameter("finalDate", finalDate);
            query.setParameter("threshold", threshold);

            logs = query.getResultList();
            tx.commit();

        } catch (HibernateException e) {
            e.printStackTrace();
            assert tx != null;
            tx.rollback();
        }


        return logs;
    }

    public Collection<AccessLog> findLocks(Collection<String> ips, Date initialDate, Date finalDate, Integer threshold) {
        Transaction tx = null;
        List<AccessLog> logs = new ArrayList<>();

        try {
            tx = getSession().beginTransaction();

            Query query = getSession().createNativeQuery("SELECT * " +
                    "FROM access_log al  " +
                    "WHERE al.access_date BETWEEN :initialData AND :finalDate " +
                    "   AND al.ip IN (:ips)" +
                    "GROUP BY al.ip HAVING count(al.ip) >= :threshold ", AccessLog.class);

            query.setParameter("initialData", initialDate);
            query.setParameter("finalDate", finalDate);
            query.setParameter("threshold", threshold);
            query.setParameterList("ips", ips);

            logs = query.getResultList();
            tx.commit();

        } catch (HibernateException e) {
            e.printStackTrace();
            assert tx != null;
            tx.rollback();
        }

        return logs;
    }

}
