# Guía de Contribución 🤝

¡Muchas gracias por tu interés en contribuir a este proyecto! Toda ayuda es bienvenida, ya sea reportando errores, sugiriendo mejoras o enviando código.

Sigue estas pautas para que el proceso de revisión y aprobación sea ágil y ordenado.

---

## Código de Conducta

Esperamos un trato respetuoso, constructivo e inclusivo entre todas las personas que participen en discusiones, revisiones e incidencias.

---

## ¿Cómo contribuir?

### 1. Reportar errores o sugerir mejoras
Antes de enviar código para una función grande o un cambio estructural, te recomendamos abrir un **Issue**:
* Describe claramente el comportamiento esperado y el comportamiento observado (si es un error).
* Incluye pasos para reproducir el problema.
* Agrega capturas de pantalla o logs si aportan contexto.

---

### 2. Flujo de trabajo para enviar código (Fork & Pull Request)

No es necesario tener permisos de escritura directos sobre este repositorio. El flujo estándar es el siguiente:

1. **Haz un Fork** de este repositorio hacia tu cuenta de GitHub.
2. **Clona tu fork** en tu máquina local:
   ```bash
   git clone https://github.com/TU-USUARIO/NOMBRE-DEL-REPOSITORIO.git
   cd NOMBRE-DEL-REPOSITORIO
   ```
3. **Crea una nueva rama** con un nombre descriptivo:
   * Para nuevas funciones: `feature/nombre-de-la-mejora`
   * Para solución de errores: `fix/descripcion-del-bug`
   ```bash
   git checkout -b feature/nombre-de-la-mejora
   ```
4. **Realiza tus cambios** respetando las buenas prácticas del proyecto:
   * Mantén los cambios acotados al objetivo del PR.
   * Escribe código limpio y comenta solo lo necesario.
   * Si el proyecto cuenta con pruebas automáticas, asegúrate de que pasen localmente.
5. **Crea commits claros y descriptivos**:
   ```bash
   git commit -m "feat: agrega soporte para autenticación con token"
   ```
6. **Sube la rama a tu fork**:
   ```bash
   git push origin feature/nombre-de-la-mejora
   ```
7. **Abre un Pull Request**:
   * Ve al repositorio original en GitHub. Verás la opción de comparar ramas y abrir un Pull Request hacia la rama `main` (o la rama de desarrollo indicada).
   * Completa la plantilla del PR con el mayor detalle posible.

---

## Proceso de Revisión

1. **Revisión del código:** Uno de los mantenedores revisará tus cambios y podrá solicitar ajustes o resolver dudas en la conversación del PR.
2. **Aprobación:** Una vez que todo esté en orden, el PR recibirá el estado de **Approved**.
3. **Merge:** Un mantenedor fusionará tu rama en el proyecto principal. ¡Tus cambios formarán parte del proyecto!

¡Gracias nuevamente por tu tiempo y dedicación!