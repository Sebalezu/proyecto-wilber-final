import os
import zipfile
import html

# Directorio de salida
OUTPUT_DIR = r"c:\Users\sebas\Desktop\la-dorada-sas\docs"
OUTPUT_FILE = os.path.join(OUTPUT_DIR, "Casos_de_Uso_La_Dorada_SAS.docx")

# Namespaces XML de WordprocessingML
W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"

def escape(text):
    return html.escape(text)

def make_p(text, style=None, bold=False, italic=False, color=None, size=None, align=None, space_after=120):
    p_pr = []
    if style:
        p_pr.append(f'<w:pStyle w:val="{style}"/>')
    if align:
        p_pr.append(f'<w:jc w:val="{align}"/>')
    if space_after is not None:
        p_pr.append(f'<w:spacing w:after="{space_after}" w:line="276" w:lineRule="auto"/>')
    
    r_pr = []
    if bold:
        r_pr.append('<w:b/>')
    if italic:
        r_pr.append('<w:i/>')
    if color:
        r_pr.append(f'<w:color w:val="{color}"/>')
    if size:
        r_pr.append(f'<w:sz w:val="{size}"/>')
        r_pr.append(f'<w:szCs w:val="{size}"/>')

    p_pr_xml = f"<w:pPr>{''.join(p_pr)}</w:pPr>" if p_pr else ""
    r_pr_xml = f"<w:rPr>{''.join(r_pr)}</w:rPr>" if r_pr else ""

    # Dividir saltos de línea si existen
    runs = []
    lines = text.split("\n")
    for i, line in enumerate(lines):
        runs.append(f'<w:r>{r_pr_xml}<w:t xml:space="preserve">{escape(line)}</w:t></w:r>')
        if i < len(lines) - 1:
            runs.append('<w:r><w:br/></w:r>')

    return f'<w:p>{p_pr_xml}{"".join(runs)}</w:p>'

def make_cell(content_p_list, width=None, bg_color=None, border=None):
    tc_pr = []
    if width:
        tc_pr.append(f'<w:tcW w:w="{width}" w:type="dxa"/>')
    if bg_color:
        tc_pr.append(f'<w:shd w:val="clear" w:color="auto" w:fill="{bg_color}"/>')
    tc_pr.append('<w:tcMar><w:top w:w="120" w:type="dxa"/><w:bottom w:w="120" w:type="dxa"/><w:left w:w="160" w:type="dxa"/><w:right w:w="160" w:type="dxa"/></w:tcMar>')
    tc_pr_xml = f"<w:tcPr>{''.join(tc_pr)}</w:tcPr>"
    return f'<w:tc>{tc_pr_xml}{"".join(content_p_list)}</w:tc>'

def make_table(rows, col_widths=None):
    tbl_pr = [
        '<w:tblPr>',
        '<w:tblStyle w:val="TableGrid"/>',
        '<w:tblW w:w="9600" w:type="dxa"/>',
        '<w:jc w:val="center"/>',
        '<w:tblBorders>',
        '<w:top w:val="single" w:sz="6" w:space="0" w:color="D6E8EB"/>',
        '<w:left w:val="single" w:sz="6" w:space="0" w:color="D6E8EB"/>',
        '<w:bottom w:val="single" w:sz="6" w:space="0" w:color="D6E8EB"/>',
        '<w:right w:val="single" w:sz="6" w:space="0" w:color="D6E8EB"/>',
        '<w:insideH w:val="single" w:sz="4" w:space="0" w:color="D6E8EB"/>',
        '<w:insideV w:val="single" w:sz="4" w:space="0" w:color="D6E8EB"/>',
        '</w:tblBorders>',
        '</w:tblPr>'
    ]
    
    grid = []
    if col_widths:
        grid.append('<w:tblGrid>')
        for w in col_widths:
            grid.append(f'<w:gridCol w:w="{w}"/>')
        grid.append('</w:tblGrid>')

    tbl_rows = []
    for r in rows:
        tbl_rows.append(f'<w:tr>{r}</w:tr>')

    return f'<w:tbl>{"".join(tbl_pr)}{"".join(grid)}{"".join(tbl_rows)}</w:tbl>'

def generate_document_xml():
    body = []

    # Portada / Cabecera Principal
    body.append(make_p("LA DORADA S.A.S.", style="Title", bold=True, color="0B6E78", size="52", align="center", space_after=60))
    body.append(make_p("Sistema de Pedidos e Inventario", bold=True, color="14508A", size="30", align="center", space_after=120))
    body.append(make_p("ESPECIFICACIÓN FORMAL DE CASOS DE USO", bold=True, color="12333A", size="26", align="center", space_after=240))
    body.append(make_p("Versión: 1.0.0   |   Fecha: Noviembre 2026   |   Estado: Implementado y Validado", italic=True, color="4F6B72", size="20", align="center", space_after=400))
    body.append(make_p("―" * 55, color="D6E8EB", align="center", space_after=300))

    # 1. Introducción y Actores
    body.append(make_p("1. Actores del Sistema", style="Heading1", bold=True, color="0B6E78", size="32", space_after=140))
    body.append(make_p("El sistema implementa un control de acceso basado en roles con responsabilidades y privilegios claramente segregados:", space_after=140))

    # Tabla de Actores
    actores_headers = (
        make_cell([make_p("Actor", bold=True, color="FFFFFF", size="20", align="center", space_after=0)], width="2200", bg_color="0B6E78") +
        make_cell([make_p("Área / Rol", bold=True, color="FFFFFF", size="20", space_after=0)], width="2200", bg_color="0B6E78") +
        make_cell([make_p("Responsabilidades y Alcance en el Sistema", bold=True, color="FFFFFF", size="20", space_after=0)], width="5200", bg_color="0B6E78")
    )
    actores_rows = [actores_headers]
    
    actores_data = [
        ("Administrador", "ADMINISTRADOR\n(Gerencia General)", "Control total del sistema. Configuración del catálogo (crear, editar, activar/desactivar productos), supervisión de pedidos, confirmación, despacho, cancelación con restitución y auditoría de Kardex."),
        ("Vendedor", "VENDEDOR\n(Fuerza Comercial)", "Atención a clientes. Consulta de catálogo y existencias en tiempo real, registro de clientes, creación de pedidos (borrador o confirmación directa), modificación de pedidos pendientes y cancelación/rechazo preventivo."),
        ("Bodega", "BODEGA\n(Almacén y Logística)", "Operaciones físicas y stock. Monitoreo de alertas de stock bajo, registro de ingreso de mercancía / ajustes de existencias (+ Stock), despacho de pedidos confirmados y cancelación de envíos con restitución automática."),
        ("Sistema", "SISTEMA\n(Motor Transaccional)", "Automatización de reglas de negocio ACID: validación en 2 fases con bloqueo pesimista (Pessimistic Lock), cálculo de totales y subtotales, restitución transaccional y consecutivos automáticos ASE-XXX.")
    ]

    for act, rol, desc in actores_data:
        r_cells = (
            make_cell([make_p(act, bold=True, color="0B6E78", size="19", space_after=0)], width="2200") +
            make_cell([make_p(rol, bold=True, color="12333A", size="18", space_after=0)], width="2200") +
            make_cell([make_p(desc, color="12333A", size="18", space_after=0)], width="5200")
        )
        actores_rows.append(r_cells)

    body.append(make_table(actores_rows, [2200, 2200, 5200]))
    body.append(make_p("", space_after=240))

    # 2. Matriz de Permisos
    body.append(make_p("2. Matriz de Casos de Uso vs. Roles", style="Heading1", bold=True, color="0B6E78", size="32", space_after=140))
    body.append(make_p("Distribución de privilegios por caso de uso en la interfaz web y endpoints de API REST:", space_after=140))

    matriz_headers = (
        make_cell([make_p("Código", bold=True, color="FFFFFF", size="19", align="center", space_after=0)], width="1200", bg_color="0B6E78") +
        make_cell([make_p("Nombre del Caso de Uso", bold=True, color="FFFFFF", size="19", space_after=0)], width="4800", bg_color="0B6E78") +
        make_cell([make_p("Admin", bold=True, color="FFFFFF", size="19", align="center", space_after=0)], width="1200", bg_color="0B6E78") +
        make_cell([make_p("Vendedor", bold=True, color="FFFFFF", size="19", align="center", space_after=0)], width="1200", bg_color="0B6E78") +
        make_cell([make_p("Bodega", bold=True, color="FFFFFF", size="19", align="center", space_after=0)], width="1200", bg_color="0B6E78")
    )
    matriz_rows = [matriz_headers]

    cu_list = [
        ("CU-01", "Iniciar Sesión (Autenticación)", "✔", "✔", "✔"),
        ("CU-02", "Cerrar Sesión", "✔", "✔", "✔"),
        ("CU-03", "Consultar Catálogo de Productos y Existencias", "✔", "✔", "✔"),
        ("CU-04", "Registrar Nuevo Producto (Consecutivo ASE-XXX)", "✔", "―", "―"),
        ("CU-05", "Modificar Producto (Precio, Nombre, Existencias)", "✔", "―", "―"),
        ("CU-06", "Activar / Desactivar Producto en Catálogo", "✔", "―", "―"),
        ("CU-07", "Registrar y Consultar Clientes", "✔", "✔", "✔"),
        ("CU-08", "Monitorear Alertas de Stock Bajo (<= 10 unidades)", "✔", "✔", "✔"),
        ("CU-09", "Registrar Ajuste de Stock (Ingreso de Mercancía)", "✔", "―", "✔"),
        ("CU-10", "Consultar Historial de Movimientos de Inventario (Kardex)", "✔", "✔", "✔"),
        ("CU-11", "Registrar Nuevo Pedido (Borrador o Confirmación Directa)", "✔", "✔", "―"),
        ("CU-12", "Consultar y Filtrar Listado de Pedidos", "✔", "✔", "✔"),
        ("CU-13", "Visualizar Detalle y Trazabilidad de Pedido (Stepper e Historial)", "✔", "✔", "✔"),
        ("CU-14", "Modificar Líneas y Cantidades de Pedido Pendiente", "✔", "✔", "―"),
        ("CU-15", "Confirmar Pedido (Descuento Atómico en 2 Fases)", "✔", "✔", "―"),
        ("CU-16", "Despachar Pedido Confirmado", "✔", "―", "✔"),
        ("CU-17", "Cancelar Pedido Confirmado (con Restitución Automática)", "✔", "―", "✔"),
        ("CU-18", "Cancelar o Rechazar Pedido Pendiente", "✔", "✔", "―")
    ]

    for cod, nom, adm, ven, bod in cu_list:
        bg = "F3FAFB" if cod in ["CU-04", "CU-09", "CU-15", "CU-17"] else "FFFFFF"
        r_cells = (
            make_cell([make_p(cod, bold=True, color="0B6E78", size="18", align="center", space_after=0)], width="1200", bg_color=bg) +
            make_cell([make_p(nom, color="12333A", size="18", space_after=0)], width="4800", bg_color=bg) +
            make_cell([make_p(adm, bold=True, color="0B6E78" if adm=="✔" else "A0B0B3", size="18", align="center", space_after=0)], width="1200", bg_color=bg) +
            make_cell([make_p(ven, bold=True, color="14508A" if ven=="✔" else "A0B0B3", size="18", align="center", space_after=0)], width="1200", bg_color=bg) +
            make_cell([make_p(bod, bold=True, color="7A5200" if bod=="✔" else "A0B0B3", size="18", align="center", space_after=0)], width="1200", bg_color=bg)
        )
        matriz_rows.append(r_cells)

    body.append(make_table(matriz_rows, [1200, 4800, 1200, 1200, 1200]))
    body.append(make_p("", space_after=240))

    # 3. Ciclo de Vida de Pedidos
    body.append(make_p("3. Diagrama de Transición de Estados", style="Heading1", bold=True, color="0B6E78", size="32", space_after=140))
    body.append(make_p("Flujo de estados implementado con control de efectos colaterales sobre inventario:", space_after=100))
    
    flujo_p = (
        "1. PENDIENTE (Borrador inicial):\n"
        "   - Creado sin descontar inventario (stock_descontado = false).\n"
        "   - Transiciones permitidas: a CONFIRMADO, a CANCELADO o a RECHAZADO.\n"
        "   - Permite modificación de líneas y recálculo de total.\n\n"
        "2. CONFIRMADO (Stock reservado):\n"
        "   - Ejecuta validación en 2 fases con bloqueo pesimista y descuenta existencias (stock_descontado = true).\n"
        "   - Registra movimientos DESCUENTO en el Kardex.\n"
        "   - Transiciones permitidas: a DESPACHADO o a CANCELADO.\n\n"
        "3. DESPACHADO (Entrega física completada):\n"
        "   - Estado terminal de éxito. No realiza movimientos adicionales porque el stock ya fue descontado.\n\n"
        "4. CANCELADO (Orden cancelada):\n"
        "   - Si proviene de CONFIRMADO: Restituye el inventario automáticamente y genera movimientos RESTITUCION.\n"
        "   - Si proviene de PENDIENTE: No altera el inventario físico.\n"
        "   - Estado terminal.\n\n"
        "5. RECHAZADO (Orden rechazada antes de confirmación):\n"
        "   - Solo aplica desde PENDIENTE. No genera movimientos de inventario.\n"
        "   - Estado terminal."
    )
    body.append(make_p(flujo_p, color="12333A", size="20", space_after=240))

    # 4. Fichas Detalladas de Casos de Uso
    body.append(make_p("4. Fichas Técnicas de Casos de Uso", style="Heading1", bold=True, color="0B6E78", size="32", space_after=180))

    casos_detalle = [
        {
            "id": "CU-01",
            "nombre": "Iniciar Sesión (Autenticación)",
            "actores": "Administrador, Vendedor, Bodega",
            "precondicion": "Usuario registrado con estado activo en la tabla usuario de la base de datos.",
            "flujo_principal": [
                "1. El usuario navega a la URL /login.",
                "2. La aplicación presenta el formulario de acceso y el panel de selección rápida de usuarios demo (Admin, Ventas, Bodega).",
                "3. El usuario ingresa su correo electrónico y contraseña (o hace clic en una credencial demo) y presiona Iniciar Sesión.",
                "4. El frontend envía la solicitud POST /api/auth/login con { email, password }.",
                "5. El backend consulta el usuario por email, valida la contraseña y confirma que activo sea true.",
                "6. El backend retorna HTTP 200 OK con { id, nombre, email, rol }.",
                "7. El frontend guarda la sesión en localStorage, actualiza el estado reactivo del usuario y redirige a /inicio."
            ],
            "flujos_alt": [
                "4a. Credenciales inválidas o usuario inactivo: El backend responde HTTP 401 Unauthorized con formato de error uniforme. El frontend muestra alerta en rojo: 'Credenciales inválidas. Por favor verifique el correo y la contraseña ingresados'."
            ],
            "postcondicion": "Sesión iniciada. Menú de navegación y permisos de botones condicionados al rol del usuario."
        },
        {
            "id": "CU-02",
            "nombre": "Cerrar Sesión",
            "actores": "Administrador, Vendedor, Bodega",
            "precondicion": "Usuario con sesión iniciada.",
            "flujo_principal": [
                "1. El usuario hace clic en el botón 'Cerrar sesión' en el encabezado superior.",
                "2. El AuthService elimina la clave de localStorage y limpia el observable usuarioActual$.",
                "3. El enrutador de Angular redirige al usuario a /login."
            ],
            "flujos_alt": [],
            "postcondicion": "Sesión destruida. Las rutas protegidas quedan inaccesibles mediante AuthGuard."
        },
        {
            "id": "CU-03",
            "nombre": "Consultar Catálogo de Productos y Existencias",
            "actores": "Administrador, Vendedor, Bodega",
            "precondicion": "Usuario autenticado.",
            "flujo_principal": [
                "1. El usuario navega a /catalogo.",
                "2. El frontend consulta GET /api/productos con los parámetros de filtro (q para texto, activo para estado).",
                "3. El backend ejecuta la búsqueda insensible a mayúsculas y acentos sobre código y nombre.",
                "4. El frontend renderiza la lista de productos indicando código, nombre, precio formateado en COP, existencias actuales y badges de nivel de stock (NORMAL, STOCK BAJO, AGOTADO)."
            ],
            "flujos_alt": [],
            "postcondicion": "Catálogo visible con actualización reactiva en pantalla de escritorio o móvil."
        },
        {
            "id": "CU-04",
            "nombre": "Registrar Nuevo Producto",
            "actores": "Administrador",
            "precondicion": "Usuario autenticado con rol ADMINISTRADOR.",
            "flujo_principal": [
                "1. El administrador hace clic en '+ Registrar Nuevo Producto' en /catalogo.",
                "2. Se despliega el modal de registro de producto.",
                "3. Ingresa nombre, precio (decimal > 0), existencias iniciales (entero >= 0) y opcionalmente código.",
                "4. Si no digita código, el backend calcula el siguiente consecutivo automático con formato ASE-XXX.",
                "5. Envía la solicitud POST /api/productos.",
                "6. El backend valida unicidad de código y restricciones CHECK de precio y existencias.",
                "7. Guarda el registro en la tabla producto y retorna HTTP 201 Created con el ProductoResponse.",
                "8. El modal se cierra, se muestra alerta de éxito y se refresca el catálogo."
            ],
            "flujos_alt": [
                "6a. Código ingresado ya existe: El backend lanza CodigoDuplicadoException (HTTP 409 Conflict). El frontend muestra alerta indicando que el código ya está en uso."
            ],
            "postcondicion": "Nuevo producto creado y disponible para órdenes de compra."
        },
        {
            "id": "CU-05",
            "nombre": "Modificar Producto (Precio, Nombre, Existencias)",
            "actores": "Administrador",
            "precondicion": "Rol ADMINISTRADOR y producto existente.",
            "flujo_principal": [
                "1. El administrador hace clic en 'Editar' en la fila del producto.",
                "2. Se precargan los valores en el modal de edición.",
                "3. Modifica los campos deseados.",
                "4. Al guardar (PUT /api/productos/{id}), el backend compara las existencias anteriores con las nuevas.",
                "5. Si hubo diferencia de existencias, genera automáticamente un registro en movimiento_inventario de tipo AJUSTE con la diferencia absoluta.",
                "6. Retorna HTTP 200 OK y el frontend actualiza la información en pantalla."
            ],
            "flujos_alt": [],
            "postcondicion": "Datos del producto actualizados y movimiento de auditoría registrado si hubo alteración de existencias."
        },
        {
            "id": "CU-06",
            "nombre": "Activar / Desactivar Producto en Catálogo",
            "actores": "Administrador",
            "precondicion": "Rol ADMINISTRADOR.",
            "flujo_principal": [
                "1. El administrador pulsa el botón 'Desactivar' o 'Activar' sobre el producto.",
                "2. El frontend envía PATCH /api/productos/{id}/activo con { activo: boolean }.",
                "3. El backend actualiza la columna activo en la tabla producto.",
                "4. El producto cambia su badge de estado de inmediato. Los productos inactivos dejan de ofrecerse en la creación de nuevos pedidos."
            ],
            "flujos_alt": [],
            "postcondicion": "Estado de disponibilidad del producto actualizado."
        },
        {
            "id": "CU-07",
            "nombre": "Registrar y Consultar Clientes",
            "actores": "Administrador, Vendedor, Bodega",
            "precondicion": "Usuario autenticado.",
            "flujo_principal": [
                "1. En el formulario de nuevo pedido o en filtros, se consumen los clientes con GET /api/clientes.",
                "2. Si se requiere un cliente no registrado, se abre el modal 'Crear Cliente Rápido'.",
                "3. El usuario suministra nombre y teléfono, y envía POST /api/clientes.",
                "4. El backend valida no vacíos y guarda en la tabla cliente.",
                "5. El nuevo cliente se selecciona automáticamente en la orden en curso."
            ],
            "flujos_alt": [],
            "postcondicion": "Cliente almacenado y vinculado al pedido."
        },
        {
            "id": "CU-08",
            "nombre": "Monitorear Alertas de Stock Bajo",
            "actores": "Administrador, Vendedor, Bodega",
            "precondicion": "Usuario autenticado.",
            "flujo_principal": [
                "1. El usuario ingresa a la vista de /inicio (Dashboard).",
                "2. El sistema consume GET /api/inventario/stock-bajo (umbral <= 10 unidades).",
                "3. Se despliega la sección de alertas prioritarias clasificadas por nivel de criticidad (Crítico <= 5 u., Advertencia 6-10 u.).",
                "4. Cada alerta indica código, nombre, existencias restantes y acceso directo al catálogo para reposición."
            ],
            "flujos_alt": [],
            "postcondicion": "Visibilidad oportuna para reabastecimiento antes de ruptura de inventario."
        },
        {
            "id": "CU-09",
            "nombre": "Registrar Ajuste de Stock (Ingreso de Mercancía)",
            "actores": "Administrador, Bodega",
            "precondicion": "Rol ADMINISTRADOR o BODEGA.",
            "flujo_principal": [
                "1. En /catalogo, el usuario hace clic en el botón '+ Stock' en la fila del producto deseado.",
                "2. Se abre el modal 'Ingreso de Mercancía / Ajuste de Stock'.",
                "3. Muestra el código, nombre y existencias actuales del producto.",
                "4. El usuario ingresa la cantidad física adicional (entero positivo > 0) y el motivo (ej. 'Llegada proveedor lote 402').",
                "5. El modal muestra en vivo la proyección: 'Nuevas existencias: X + Y unidades'.",
                "6. El usuario confirma el ingreso enviando POST /api/inventario/ajuste con { productoId, cantidad, motivo }.",
                "7. El backend adquiere bloqueo pesimista sobre el producto, suma las existencias y crea un movimiento en movimiento_inventario con tipo AJUSTE.",
                "8. Retorna HTTP 200 OK con el producto actualizado.",
                "9. El frontend actualiza en tiempo real las existencias en la tabla y muestra notificación de éxito."
            ],
            "flujos_alt": [
                "4a. Cantidad menor o igual a cero: Validación en cliente y servidor rechaza la transacción con HTTP 400 Bad Request."
            ],
            "postcondicion": "Existencias físicas incrementadas y movimiento de auditoría registrado."
        },
        {
            "id": "CU-10",
            "nombre": "Consultar Historial de Movimientos de Inventario (Kardex)",
            "actores": "Administrador, Vendedor, Bodega",
            "precondicion": "Usuario autenticado.",
            "flujo_principal": [
                "1. Se consulta el endpoint GET /api/inventario/movimientos permitiendo filtros por productoId, pedidoId y tipo (DESCUENTO, RESTITUCION, AJUSTE).",
                "2. Retorna el historial ordenado descendentemente por fecha con la traza completa de auditoría."
            ],
            "flujos_alt": [],
            "postcondicion": "Trazabilidad completa de auditoría del inventario."
        },
        {
            "id": "CU-11",
            "nombre": "Registrar Nuevo Pedido",
            "actores": "Administrador, Vendedor",
            "precondicion": "Rol ADMINISTRADOR o VENDEDOR. Clientes y productos activos existentes.",
            "flujo_principal": [
                "1. El usuario accede a /nuevo-pedido.",
                "2. Selecciona el cliente destino.",
                "3. Agrega las líneas de pedido con el selector de producto y cantidad requerida (> 0).",
                "4. La interfaz calcula en tiempo real los subtotales y el total estimado.",
                "5. El usuario selecciona una de dos modalidades:",
                "   a) Guardar como Pendiente: Envía POST /api/pedidos con confirmar=false. El pedido nace en estado PENDIENTE sin descontar stock.",
                "   b) Confirmar Pedido: Envía POST /api/pedidos con confirmar=true. El backend ejecuta la validación en 2 fases; si hay stock, descuenta existencias de inmediato y nace en estado CONFIRMADO.",
                "6. El backend persiste la orden y retorna HTTP 201 Created con el PedidoDetalleResponse.",
                "7. El frontend navega directamente al detalle del pedido /pedidos/{id}."
            ],
            "flujos_alt": [
                "5b. Stock insuficiente al confirmar: El backend revierte la transacción y retorna HTTP 409 Conflict con la lista de productos insuficientes. El frontend presenta el banner rojo #FDE5E3 detallando: 'Producto X: pediste N y hay M disponibles'. La orden no se crea y el stock permanece intacto."
            ],
            "postcondicion": "Pedido registrado y visible en el listado histórico."
        },
        {
            "id": "CU-12",
            "nombre": "Consultar y Filtrar Listado de Pedidos",
            "actores": "Administrador, Vendedor, Bodega",
            "precondicion": "Usuario autenticado.",
            "flujo_principal": [
                "1. El usuario accede a /pedidos.",
                "2. Se listan las órdenes recuperadas de GET /api/pedidos.",
                "3. El usuario puede filtrar por estado mediante píldoras interactivas (TODOS, PENDIENTE, CONFIRMADO, DESPACHADO, RECHAZADO, CANCELADO) o por cliente.",
                "4. En pantallas grandes se muestra la tabla detallada; en dispositivos móviles se muestran tarjetas táctiles optimizadas.",
                "5. Al hacer clic sobre cualquier fila o tarjeta, se redirige a /pedidos/:id."
            ],
            "flujos_alt": [],
            "postcondicion": "Listado de pedidos filtrado y navegación disponible."
        },
        {
            "id": "CU-13",
            "nombre": "Visualizar Detalle y Trazabilidad de Pedido",
            "actores": "Administrador, Vendedor, Bodega",
            "precondicion": "Usuario autenticado.",
            "flujo_principal": [
                "1. El usuario navega a /pedidos/:id.",
                "2. El sistema consulta GET /api/pedidos/{id}.",
                "3. Presenta los datos del cliente, estado actual con badge de color institucional y monto total.",
                "4. Muestra el Stepper / Línea de tiempo visual del ciclo de vida.",
                "5. Despliega la tabla de líneas de productos (código, nombre, precio unitario, cantidad, subtotal).",
                "6. Despliega el Historial cronológico de cambios de estado con fecha y hora exacta."
            ],
            "flujos_alt": [
                "2a. Pedido no encontrado: La API responde HTTP 404 Not Found y el frontend notifica que la orden no existe."
            ],
            "postcondicion": "Detalle completo y trazabilidad auditada en pantalla."
        },
        {
            "id": "CU-14",
            "nombre": "Modificar Líneas de Pedido Pendiente",
            "actores": "Administrador, Vendedor",
            "precondicion": "Pedido en estado PENDIENTE y rol ADMINISTRADOR o VENDEDOR.",
            "flujo_principal": [
                "1. En el detalle del pedido pendiente, el usuario presiona 'Modificar Líneas'.",
                "2. Se habilita el editor interactivo con los productos y cantidades actuales.",
                "3. El usuario agrega o elimina productos y ajusta cantidades.",
                "4. Presiona 'Guardar Cambios'.",
                "5. El frontend envía PUT /api/pedidos/{id} con las nuevas líneas.",
                "6. El backend valida que el pedido esté en PENDIENTE, recalcula precios y total, actualiza la base de datos y retorna HTTP 200 OK.",
                "7. El frontend desactiva el modo edición y muestra las líneas y total recalculados."
            ],
            "flujos_alt": [
                "5a. El pedido ya no está en estado PENDIENTE: El backend lanza PedidoNoEditableException (HTTP 409 Conflict) rechazando la modificación."
            ],
            "postcondicion": "Líneas de la orden actualizadas y total recalculado por el servidor."
        },
        {
            "id": "CU-15",
            "nombre": "Confirmar Pedido (Descuento de Stock en 2 Fases)",
            "actores": "Administrador, Vendedor",
            "precondicion": "Pedido en estado PENDIENTE.",
            "flujo_principal": [
                "1. El usuario hace clic en 'Confirmar Pedido' en /pedidos/:id.",
                "2. Confirma en el cuadro de diálogo.",
                "3. El frontend envía PATCH /api/pedidos/{id}/estado con { estado: 'CONFIRMADO' }.",
                "4. El backend adquiere bloqueo pesimista sobre el pedido y ejecuta el descuento en 2 Fases:",
                "   - Fase 1: Agrupa líneas por productoId, bloquea cada producto con SELECT FOR UPDATE y valida existencias de todos.",
                "   - Fase 2: Si todo alcanza, descuenta el stock de cada producto y crea los registros DESCUENTO en movimiento_inventario.",
                "5. Cambia el estado a CONFIRMADO, marca stock_descontado = true y registra el cambio en el historial.",
                "6. Retorna HTTP 200 OK con el pedido actualizado.",
                "7. El frontend actualiza el stepper, el badge de estado y las acciones disponibles."
            ],
            "flujos_alt": [
                "4a. Stock insuficiente en uno o más productos: El backend lanza StockInsuficienteException (HTTP 409 Conflict) con todos los detalles de los productos sin existencias suficientes. No se descuenta ningún producto. El frontend muestra el banner rojo de error #FDE5E3."
            ],
            "postcondicion": "Pedido en estado CONFIRMADO con existencias descontadas en bodega."
        },
        {
            "id": "CU-16",
            "nombre": "Despachar Pedido Confirmado",
            "actores": "Administrador, Bodega",
            "precondicion": "Pedido en estado CONFIRMADO y rol ADMINISTRADOR o BODEGA.",
            "flujo_principal": [
                "1. El usuario presiona 'Despachar Pedido' en /pedidos/:id.",
                "2. Confirma que la mercancía ha sido embalada y enviada.",
                "3. El frontend envía PATCH /api/pedidos/{id}/estado con { estado: 'DESPACHADO' }.",
                "4. El backend valida la transición, actualiza el estado a DESPACHADO y registra en el historial.",
                "5. Retorna HTTP 200 OK.",
                "6. El stepper avanza al paso final 'Despachado' y la orden queda en estado terminal cerrado (sin más acciones permitidas)."
            ],
            "flujos_alt": [],
            "postcondicion": "Pedido completado y cerrado exitosamente."
        },
        {
            "id": "CU-17",
            "nombre": "Cancelar Pedido Confirmado (con Restitución de Stock)",
            "actores": "Administrador, Bodega",
            "precondicion": "Pedido en estado CONFIRMADO y rol ADMINISTRADOR o BODEGA.",
            "flujo_principal": [
                "1. El usuario presiona 'Cancelar Pedido (Restituye Stock)'.",
                "2. El sistema despliega el cuadro de confirmación con la advertencia obligatoria:",
                "   '¿Desea cancelar el pedido? El inventario reservado será restituido a bodega automáticamente.'",
                "3. El usuario confirma la cancelación.",
                "4. El frontend envía PATCH /api/pedidos/{id}/estado con { estado: 'CANCELADO' }.",
                "5. El backend detecta que stock_descontado es true, bloquea los productos e invoca inventarioService.restituir.",
                "6. Se reintegran las cantidades exactas a la tabla producto y se registran movimientos RESTITUCION en movimiento_inventario.",
                "7. El pedido pasa a CANCELADO con stock_descontado = false y se registra en el historial.",
                "8. Retorna HTTP 200 OK.",
                "9. El frontend actualiza el stepper al banner de 'Pedido CANCELADO' y el historial refleja la restitución."
            ],
            "flujos_alt": [],
            "postcondicion": "Pedido cancelado en estado terminal y productos restituidos de inmediato al inventario disponible."
        },
        {
            "id": "CU-18",
            "nombre": "Cancelar o Rechazar Pedido Pendiente",
            "actores": "Administrador, Vendedor",
            "precondicion": "Pedido en estado PENDIENTE.",
            "flujo_principal": [
                "1. El usuario hace clic en 'Rechazar Pedido' o 'Cancelar Pedido'.",
                "2. Confirma la acción.",
                "3. El frontend envía PATCH /api/pedidos/{id}/estado con RECHAZADO o CANCELADO.",
                "4. El backend actualiza el estado y registra el historial sin tocar las existencias ni crear movimientos de inventario.",
                "5. Retorna HTTP 200 OK y el frontend muestra el pedido en estado terminal cerrado."
            ],
            "flujos_alt": [],
            "postcondicion": "Pedido cerrado sin afectación de existencias en bodega."
        }
    ]

    for cu in casos_detalle:
        body.append(make_p(f"{cu['id']}: {cu['nombre']}", style="Heading2", bold=True, color="0B6E78", size="26", space_after=100))
        
        cu_table_rows = []
        
        # Fila Actores y Precondición
        r1 = (
            make_cell([make_p("Actores:", bold=True, color="0B6E78", size="18", space_after=0)], width="2200", bg_color="F3FAFB") +
            make_cell([make_p(cu["actores"], color="12333A", size="18", space_after=0)], width="7400")
        )
        cu_table_rows.append(r1)

        r2 = (
            make_cell([make_p("Precondición:", bold=True, color="0B6E78", size="18", space_after=0)], width="2200", bg_color="F3FAFB") +
            make_cell([make_p(cu["precondicion"], color="12333A", size="18", space_after=0)], width="7400")
        )
        cu_table_rows.append(r2)

        # Flujo Principal
        flujo_p_lines = "\n".join(cu["flujo_principal"])
        r3 = (
            make_cell([make_p("Flujo Principal:", bold=True, color="0B6E78", size="18", space_after=0)], width="2200", bg_color="F3FAFB") +
            make_cell([make_p(flujo_p_lines, color="12333A", size="18", space_after=0)], width="7400")
        )
        cu_table_rows.append(r3)

        # Flujos Alternativos / Excepciones
        alt_text = "\n".join(cu["flujos_alt"]) if cu["flujos_alt"] else "Ninguno identificado (flujo nominal directo)."
        r4 = (
            make_cell([make_p("Excepciones / Flujos Alt.:", bold=True, color="0B6E78", size="18", space_after=0)], width="2200", bg_color="F3FAFB") +
            make_cell([make_p(alt_text, color="8F2A21" if cu["flujos_alt"] else "12333A", size="18", space_after=0)], width="7400")
        )
        cu_table_rows.append(r4)

        # Postcondición
        r5 = (
            make_cell([make_p("Postcondición:", bold=True, color="0B6E78", size="18", space_after=0)], width="2200", bg_color="F3FAFB") +
            make_cell([make_p(cu["postcondicion"], bold=True, color="14508A", size="18", space_after=0)], width="7400")
        )
        cu_table_rows.append(r5)

        body.append(make_table(cu_table_rows, [2200, 7400]))
        body.append(make_p("", space_after=200))

    # Pie de documento
    body.append(make_p("―" * 55, color="D6E8EB", align="center", space_after=140))
    body.append(make_p("La Dorada S.A.S.  |  Todo limpio, todo en orden  |  Documento Técnico Aprobado", italic=True, color="4F6B72", size="18", align="center", space_after=0))

    body_xml = "".join(body)

    document_xml = (
        '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
        f'<w:document xmlns:w="{W_NS}">\n'
        f'<w:body>{body_xml}'
        '<w:sectPr><w:pgSz w:w="12240" w:h="15840"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440"/></w:sectPr>'
        '</w:body></w:document>'
    )
    return document_xml

def generate_styles_xml():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Calibri"/>
        <w:sz w:val="22"/>
        <w:szCs w:val="22"/>
        <w:color w:val="12333A"/>
      </w:rPr>
    </w:rPrDefault>
  </w:docDefaults>
  <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
    <w:name w:val="Normal"/>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr><w:spacing w:before="240" w:after="120"/><w:keepNext/></w:pPr>
    <w:rPr><w:b/><w:sz w:val="32"/><w:color w:val="0B6E78"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="heading 2"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr><w:spacing w:before="180" w:after="80"/><w:keepNext/></w:pPr>
    <w:rPr><w:b/><w:sz w:val="26"/><w:color w:val="0B6E78"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Title">
    <w:name w:val="Title"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr><w:spacing w:before="0" w:after="100"/><w:jc w:val="center"/></w:pPr>
    <w:rPr><w:b/><w:sz w:val="52"/><w:color w:val="0B6E78"/></w:rPr>
  </w:style>
  <w:style w:type="table" w:default="1" w:styleId="TableGrid">
    <w:name w:val="Table Grid"/>
    <w:tblPr>
      <w:tblBorders>
        <w:top w:val="single" w:sz="4" w:space="0" w:color="D6E8EB"/>
        <w:left w:val="single" w:sz="4" w:space="0" w:color="D6E8EB"/>
        <w:bottom w:val="single" w:sz="4" w:space="0" w:color="D6E8EB"/>
        <w:right w:val="single" w:sz="4" w:space="0" w:color="D6E8EB"/>
        <w:insideH w:val="single" w:sz="4" w:space="0" w:color="D6E8EB"/>
        <w:insideV w:val="single" w:sz="4" w:space="0" w:color="D6E8EB"/>
      </w:tblBorders>
    </w:tblPr>
  </w:style>
</w:styles>'''

def generate_content_types_xml():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>'''

def generate_package_rels_xml():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>'''

def generate_document_rels_xml():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>'''

def generate_core_props_xml():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>Especificación de Casos de Uso - La Dorada S.A.S.</dc:title>
  <dc:creator>La Dorada S.A.S.</dc:creator>
  <cp:lastModifiedBy>La Dorada S.A.S.</cp:lastModifiedBy>
  <dcterms:created xsi:type="dcterms:W3CDTF">2026-11-09T12:00:00Z</dcterms:created>
  <dcterms:modified xsi:type="dcterms:W3CDTF">2026-11-09T12:00:00Z</dcterms:modified>
</cp:coreProperties>'''

def generate_app_props_xml():
    return '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">
  <Application>Microsoft Word</Application>
</Properties>'''

def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    
    with zipfile.ZipFile(OUTPUT_FILE, 'w', zipfile.ZIP_DEFLATED) as docx:
        docx.writestr('[Content_Types].xml', generate_content_types_xml())
        docx.writestr('_rels/.rels', generate_package_rels_xml())
        docx.writestr('docProps/core.xml', generate_core_props_xml())
        docx.writestr('docProps/app.xml', generate_app_props_xml())
        docx.writestr('word/_rels/document.xml.rels', generate_document_rels_xml())
        docx.writestr('word/styles.xml', generate_styles_xml())
        docx.writestr('word/document.xml', generate_document_xml())

    print(f"[OK] Archivo Word generado exitosamente en: {OUTPUT_FILE}")
    print(f"Tamano del archivo: {os.path.getsize(OUTPUT_FILE)} bytes")

if __name__ == "__main__":
    main()
