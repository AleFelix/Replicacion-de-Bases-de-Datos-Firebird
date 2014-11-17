package proxy;


public class QueueWriter implements Runnable {
	
	
	private Thread threadAnterior;
	private Cola<String> queue;
	private String instruccion;
	
	public QueueWriter(Runnable threadAnterior,Cola<String> tQueue,String instruccion ) {
		
		//Indico cual es el thread anterior
		this.threadAnterior=(Thread) threadAnterior;
		//Intancio la cola
		this.queue = tQueue;
		//Intancio la instruccion
		this.instruccion = instruccion;
		
	}
	
	@Override
	public void run(){
		System.out.println("Corriendo QueueWriter");
		/**
		 * Si el hilo anterior todavia esta corriendo , lo espero a que finalize y 
		 * luego escribo en la cola
		 */
		try {
			if(threadAnterior != null){
				if (threadAnterior.isAlive()){
					threadAnterior.join();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//synchronized (queue.getClass()){
			System.out.println("Encolando instruccion desde QueueWriter");
			queue.encolar(instruccion);
			System.out.println("Instruccion encolada desde QueueWriter");
		//}
	}


}
