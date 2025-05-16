import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, catchError, Observable, switchMap, throwError} from 'rxjs';
import { map, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class DataFromFormService {
  private apiUrl = 'http://localhost:8080/EasyTask-1.0-SNAPSHOT/api';
  calendarSessionsSubject = new BehaviorSubject<any[]>(this.loadSessionsFromStorage());
  calendarSessions$ = this.calendarSessionsSubject.asObservable();


  tasksCache: { [key: string]: BehaviorSubject<any[]> } = {
    personal: new BehaviorSubject<any[]>([]),
    shared: new BehaviorSubject<any[]>([]),
    group: new BehaviorSubject<any[]>([]),
  };

  constructor(private http: HttpClient) {}


  private normalizeSessions(task: any, type: keyof typeof this.tasksCache): any[] {
    type = type as string;
    type = type.toLowerCase()
    console.log('normalizeSessions type:'+ type)
    // Crea una mappa per i dati dei subtasks: sessionId -> {subtaskId, subtaskName}
    const subtaskMap = new Map<number, { subtaskId: number, subtaskName: string }>();
    if (task.subtasks && Array.isArray(task.subtasks)) {
      for (const subtask of task.subtasks) {
        if (subtask.sessions && Array.isArray(subtask.sessions)) {
          for (const subSession of subtask.sessions) {
            subtaskMap.set(subSession.id, {
              subtaskId: subtask.id,
              subtaskName: subtask.name
            });
          }
        }
      }
    }
    // Mappa le sessioni aggiungendo le informazioni del task e del subtask (se presenti)
    return task.sessions.map((session: any) => {
      const subtaskInfo = subtaskMap.get(session.id);
      return {
        ...session,
        taskId: task.id,
        taskName: task.name,
        taskType: type,
        subtaskId: subtaskInfo?.subtaskId ?? null,
        subtaskName: subtaskInfo?.subtaskName ?? null
      };
    });
  }


  deleteInProgressSessionsFromCalendar(taskId: number, type: keyof typeof this.tasksCache): Observable<any[]> {
    type = type as string;
    type = type.toLowerCase()
    return this.getTask(type, taskId).pipe(
      map((oldTask: any) => {
        if (!oldTask || !oldTask.sessions) {
          console.warn(`⚠️ Nessuna sessione trovata per il task con ID ${taskId}`);
          return this.calendarSessionsSubject.getValue();
        }
        const currentSessions = this.calendarSessionsSubject.getValue();
        const newSessions = currentSessions.filter((session: any) => session.taskId !== taskId);

        this.calendarSessionsSubject.next(newSessions);
        localStorage.setItem('calendarSessions', JSON.stringify(newSessions));

        return newSessions;
      }),
      catchError((error) => {
        console.error(`❌ Errore nella rimozione delle sessioni dal calendario per il task ID ${taskId}:`, error);
        return throwError(() => new Error(`Errore nella rimozione delle sessioni: ${error.message || error}`));
      })
    );
  }


  // Metodo per caricare le sessioni dal localStorage
  private loadSessionsFromStorage(): any[] {
    const sessions = localStorage.getItem('calendarSessions');
    return sessions ? JSON.parse(sessions) : [];
  }

  /** Recupera i task dal backend per un tipo specifico **/
  getTasks(type: keyof typeof this.tasksCache): Observable<any[]> {
    type = type as string;
    type = type.toLowerCase()
    return this.http.get<any[]>(`${this.apiUrl}/${type}`).pipe(
      tap((tasks) => this.tasksCache[type].next(tasks)), // Aggiorna il BehaviorSubject
      map(() => this.tasksCache[type].getValue())
    );
  }

  getAllSessions(type: keyof typeof this.tasksCache): Observable<any[]> {
    const lowerType = (type as string).toLowerCase();
    return this.http.get<any[]>(`${this.apiUrl}/${lowerType}/sessions`).pipe(
      tap((sessions: any[]) => {
        // Aggiorno Subject e localStorage
        this.calendarSessionsSubject.next(sessions);
        localStorage.setItem('calendarSessions', JSON.stringify(sessions));
      })
    );
  }


  getTask(type: keyof typeof this.tasksCache, taskId: number): Observable<any> {
    type = type as string;
    type = type.toLowerCase()
    return this.http.get<any>(`${this.apiUrl}/${type}/${taskId}`).pipe(
      tap((task) => {
        console.log(`✅ Task ricevuto:`, task);
      }),
      catchError((error) => {
        console.error(`❌ Errore nel recupero del task con ID ${taskId}:`, error);
        return throwError(() => error);
      })
    );
  }


  moveToCalendarTask(type: keyof typeof this.tasksCache, taskId: number): Observable<any> {
    type = type as string;
    type = type.toLowerCase()
    console.log('moveToCalendarTask type:'+ type);
    return this.http.put<any>(`${this.apiUrl}/${type}/moveToCalendar?personalId=${taskId}`, {})
      .pipe(
        tap(response => {
          console.log("Response dalla PUT:", response);
        }),
        switchMap(() => this.getTask(type, taskId)),
        tap(updatedTask => {
          if (updatedTask && updatedTask.sessions) {
            console.log('moveToCalendarTask type part 2:'+ type);
            const extendedSessionsFromTask = this.normalizeSessions(updatedTask, type as string);
            const current = this.calendarSessionsSubject.getValue();
            const updatedSessions = [...current, ...extendedSessionsFromTask];

            this.calendarSessionsSubject.next(updatedSessions);
            localStorage.setItem('calendarSessions', JSON.stringify(updatedSessions));
          } else {
            console.error('La risposta dal server non contiene le sessioni attese:', updatedTask);
          }
        }),
        catchError((error) => {
          console.error(`❌ Errore durante il processo per il task con ID ${taskId}:`, error);
          return throwError(() => error);
        })
      );
  }




  /** elimina un task specifico **/
  deleteTask(type: keyof typeof this.tasksCache, taskId: number): Observable<void> {
    type = type as string;
    type = type.toLowerCase()
    return this.http.delete<void>(`${this.apiUrl}/${type}/${taskId}`).pipe(
      tap(() => {
        // Rimuove il task dalla cache locale
        const updatedTasks = this.tasksCache[type].getValue().filter(task => task.id !== taskId);
        this.tasksCache[type].next(updatedTasks);
      })
    );
  }

  editTask(type: keyof typeof this.tasksCache, taskId: number, payload: any): Observable<any> {
    type = type as string;
    type = type.toLowerCase()
    return this.http.put<any>(`${this.apiUrl}/${type}/${taskId}`, payload).pipe(
      tap((updatedTask: any) => {
        console.log('Task modified:', updatedTask.name);
        // Aggiorna la cache sostituendo il task modificato
        const currentTasks = this.tasksCache[type].getValue();
        const index = currentTasks.findIndex(task => task.id === updatedTask.id);
        if (index !== -1) {
          currentTasks[index] = updatedTask;
          this.tasksCache[type].next([...currentTasks]);
        }
        // Se il task aggiornato contiene sessioni, normalizza le sessioni (includendo i dati di subtask)
        if (updatedTask && updatedTask.sessions && Array.isArray(updatedTask.sessions)) {
          const extendedSessionsFromTask = this.normalizeSessions(updatedTask, type.toString());

          // Aggiorna le sessioni del calendario:
          // Per ogni sessione attualmente presente, se appartiene al task aggiornato, la sostituisce con quella normalizzata
          const currentCalendarSessions = this.calendarSessionsSubject.getValue();
          const updatedCalendarSessions = currentCalendarSessions.map((session: any) => {
            if (session.taskId === updatedTask.id) {
              // Cerca la sessione normalizzata che corrisponde all'attuale (basandosi sull'id)
              const normalizedSession = extendedSessionsFromTask.find((s: any) => s.id === session.id);
              if (normalizedSession) {
                return normalizedSession;
              }
            }
            return session;
          });

          // Re-emetti l'array aggiornato e salva in localStorage
          this.calendarSessionsSubject.next(updatedCalendarSessions);
          localStorage.setItem('calendarSessions', JSON.stringify(updatedCalendarSessions));
        }
      })
    );
  }

  completeSession(type: keyof typeof this.tasksCache, taskId: number, sessionId: number): Observable<any> {
    type = type as string;
    type = type.toLowerCase()
    return this.http.put<any>(`${this.apiUrl}/${type}/completeSession/${taskId}?sessionId=${sessionId}`, {})
      .pipe(
        tap(() => {
          // Aggiorna lo stato della sessione nel BehaviorSubject
          let currentSessions = this.calendarSessionsSubject.getValue();
          let index = currentSessions.findIndex((s: any) => s.id === sessionId);
          if (index !== -1) {
            currentSessions[index] = {
              ...currentSessions[index],
              state: 'COMPLETED'
            };
          }
          this.calendarSessionsSubject.next([...currentSessions]);
          localStorage.setItem('calendarSessions', JSON.stringify(currentSessions));
        }),
        switchMap(() => this.getTask(type, taskId)),
        tap((updatedTask: any) => {
          // Aggiorna la cache dei task
          let currentTasks = this.tasksCache[type].getValue();
          currentTasks = currentTasks.filter((task: any) => task.id !== updatedTask.id);
          currentTasks.push(updatedTask);
          this.tasksCache[type].next(currentTasks);

          // Se il task aggiornato contiene sessioni, normalizza le sessioni per aggiornare anche i campi extra
          if (updatedTask && updatedTask.sessions && Array.isArray(updatedTask.sessions)) {
            const normalizedSessions = this.normalizeSessions(updatedTask, type.toString());
            let currentCalendarSessions = this.calendarSessionsSubject.getValue();
            // Sostituisci le sessioni appartenenti al task aggiornato con quelle normalizzate
            currentCalendarSessions = currentCalendarSessions.map((session: any) => {
              if (session.taskId === updatedTask.id) {
                const normalized = normalizedSessions.find((ns: any) => ns.id === session.id);
                return normalized ? normalized : session;
              }
              return session;
            });
            this.calendarSessionsSubject.next(currentCalendarSessions);
            localStorage.setItem('calendarSessions', JSON.stringify(currentCalendarSessions));
          }
        })
      );
  }

  freezeTaskFromCalendar(type: keyof typeof this.tasksCache,personalId: number ):Observable<void>{
    type = type as string;
    type = type.toLowerCase()
    return this.http.put<any>(`${this.apiUrl}/${type}/freeze/${personalId}`, {}).pipe(
      switchMap(() =>this.getTask(type, personalId)),
      tap((updatedTask: any) => {
        let currentTasks = this.tasksCache[type].getValue();
        currentTasks = currentTasks.filter((task: any) => updatedTask.id !== task.id);
        currentTasks.push(updatedTask);
        this.tasksCache[type].next(currentTasks);
        if (updatedTask && updatedTask.sessions && Array.isArray(updatedTask.sessions)) {
          const normalizedSessions = this.normalizeSessions(updatedTask, type.toString());
          let currentCalendarSessions = this.calendarSessionsSubject.getValue();
          // Sostituisci le sessioni appartenenti al task aggiornato con quelle normalizzate
          currentCalendarSessions = currentCalendarSessions.map((session: any) => {
            if (session.taskId === updatedTask.id) {
              const normalized = normalizedSessions.find((ns: any) => ns.id === session.id);
              return normalized ? normalized : session;
            }
            return session;
          });
          this.calendarSessionsSubject.next(currentCalendarSessions);
          localStorage.setItem('calendarSessions', JSON.stringify(currentCalendarSessions));
        }
      })
    )
  }



}
