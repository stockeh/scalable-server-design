package cs455.scaling.server;

import java.util.concurrent.LinkedBlockingQueue;

class WorkerThread extends Thread {

  private final LinkedBlockingQueue<Task> queue;

  public WorkerThread(LinkedBlockingQueue<Task> queue) {
    this.queue = queue;
  }

  public void run() {
    Task task;

    while ( true )
    {
      try
      {
        task = queue.take();
      } catch ( InterruptedException e )
      {
        e.printStackTrace();
        return;
      }
      try
      {
        task.run();
      } catch ( RuntimeException e )
      {
        System.out.println(
            "Thread pool is interrupted due to an issue: " + e.getMessage() );
      }
    }
  }
}
