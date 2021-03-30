package org.acme.micrometer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/")
public class DbCnxnViaHibernate {
    private static final String className = DbCnxnViaHibernate.class.getName();

    @Inject @ApplicationScoped
    MeterRegistry registry;

    private Counter totalBuildsCounter;

    @PostConstruct
    void init() {
        totalBuildsCounter = registry.counter(className + ".total.builds.count");
        totalBuildsCounter.increment(getLatestTotalCountOfBuilds());
    }

    private int getLatestTotalCountOfBuilds() {
        Integer count = 0;
        Session session = null;
        try {
            SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
            if (sessionFactory == null) {
                System.out.println("buildSessionFactory() returned null");
                return -1;
            }
            session = sessionFactory.openSession();
            if (session == null) {
                System.out.println("sessionFactory.openSession() returned null");
                return -1;
            }
            session.beginTransaction();
            String qry = "SELECT count(*) FROM _archived_buildrecords WHERE temporarybuild == FALSE";
            Query countQry = session.createQuery(qry);
            List<Integer> rs = countQry.getResultList();
            count = rs.get(0);
            System.out.println("Records count = " + count);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            System.out.println("Failed to create sessionFactory object - " + e.getCause());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return count;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/count")
    public double getCurrentTotalBuildsCount() {
        totalBuildsCounter.increment();
        System.out.println("getCurrentTotalBuildsCount = " + totalBuildsCounter.count());
        return totalBuildsCounter.count();
    }
}