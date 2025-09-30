"""
Enhanced Animal Shelter Dashboard with Edit & Dark Mode
"""

import dash
from dash import dcc, html, dash_table, Input, Output, State
import dash_leaflet as dl
import plotly.express as px
import pandas as pd
import base64
import os
from dash.exceptions import PreventUpdate

from animalShelter import AnimalShelter

USERNAME = None
PASSWORD = None
HOST = 'localhost'
PORT = 27017

print("Connecting to database...")
db = AnimalShelter(username=USERNAME, password=PASSWORD, host=HOST, port=PORT)

print("Loading data...")
df = pd.DataFrame(db.read({}))

if df.empty:
    print("WARNING: No data found! Make sure your MongoDB has data.")
else:
    print(f"✓ Loaded {len(df)} records")

app = dash.Dash(__name__)
app.title = "Animal Shelter Dashboard"

light_theme = {
    'background': '#f8f9fa',
    'card': '#ffffff',
    'primary': '#2c3e50',
    'secondary': '#3498db',
    'accent': '#e74c3c',
    'success': '#27ae60',
    'text': '#2c3e50',
    'text_secondary': '#6c757d',
    'border': '#dee2e6',
    'hover': '#f1f3f5',
    'table_header': '#2c3e50',
    'table_selected': '#3498db'
}

dark_theme = {
    'background': '#1a1a1a',
    'card': '#2d2d2d',
    'primary': '#5dade2',
    'secondary': '#2980b9',
    'accent': '#e74c3c',
    'success': '#2ecc71',
    'text': '#ffffff',
    'text_secondary': '#b0b0b0',
    'border': '#404040',
    'hover': '#3a3a3a',
    'table_header': '#1e5a8e',
    'table_selected': '#2874a6'
}

"""
function load_logo
params: none
Description: Load and encode logo image if it exists
"""
def load_logo():
    try:
        if os.path.exists('grazioso_logo.png'):
            with open('grazioso_logo.png', 'rb') as f:
                encoded = base64.b64encode(f.read()).decode()
            return f'data:image/png;base64,{encoded}'
    except:
        pass
    return None

"""
function get_theme_styles
params: is_dark (bool)
Description: Returns theme dictionary based on dark mode state
"""
def get_theme_styles(is_dark):
    return dark_theme if is_dark else light_theme

app.layout = html.Div(
    id='main-container',
    children=[
        dcc.Store(id='theme-store', data={'dark': False}),
        dcc.Store(id='selected-animal-store', data=None),
        
        html.Div(
            id='header-section',
            children=[
                html.Div(
                    style={'display': 'flex', 'justifyContent': 'space-between', 'alignItems': 'center'},
                    children=[
                        html.Div([
                            html.H1('Grazioso Salvare - Animal Shelter Dashboard', style={'margin': '0'}),
                            html.P('Enhanced Local Desktop Version', style={'marginTop': '10px', 'fontSize': '16px'})
                        ]),
                        html.Div([
                            html.Button(
                                '🌙 Dark Mode',
                                id='theme-toggle',
                                n_clicks=0,
                                style={
                                    'padding': '10px 20px',
                                    'fontSize': '16px',
                                    'border': 'none',
                                    'borderRadius': '25px',
                                    'cursor': 'pointer',
                                    'fontWeight': 'bold',
                                    'boxShadow': '0 2px 4px rgba(0,0,0,0.2)',
                                    'transition': 'all 0.3s'
                                }
                            ),
                            html.Img(src=load_logo(), style={'height': '80px', 'marginLeft': '20px'}) if load_logo() else None
                        ], style={'display': 'flex', 'alignItems': 'center'})
                    ]
                )
            ]
        ),

        html.Div(id='stats-section'),

        html.Div(
            id='filters-section',
            children=[
                html.H3('Filters', style={'marginBottom': '15px'}),
                html.Div([
                    html.Label('Animal Type:', style={'fontWeight': 'bold', 'display': 'block', 'marginBottom': '5px'}),
                    dcc.Dropdown(
                        id='filter-type',
                        options=[{'label': t, 'value': t} for t in sorted(df['animal_type'].dropna().unique())] if not df.empty else [],
                        value=None,
                        multi=True,
                        placeholder="Select animal types"
                    )
                ], style={'marginBottom': '15px'}),
                
                html.Div([
                    html.Label('Breed:', style={'fontWeight': 'bold', 'display': 'block', 'marginBottom': '5px'}),
                    dcc.Dropdown(
                        id='filter-breed',
                        options=[],
                        value=None,
                        multi=True,
                        placeholder="Select breeds"
                    )
                ], style={'marginBottom': '15px'}),
                
                html.Button('Reset All Filters', id='reset-btn', n_clicks=0, className='action-button')
            ]
        ),

        html.Div(
            id='table-section',
            children=[
                html.H3('Animal Records', style={'marginBottom': '15px'}),
                dash_table.DataTable(
                    id='datatable-id',
                    columns=[{"name": i, "id": i} for i in df.columns] if not df.empty else [],
                    data=df.to_dict('records') if not df.empty else [],
                    page_size=15,
                    filter_action='native',
                    sort_action='native',
                    row_selectable='single',
                    selected_rows=[],
                    style_table={'overflowX': 'auto'}
                ),
                html.Div([
                    html.Button('✏️ Edit Selected Animal', id='edit-btn', n_clicks=0, className='action-button', style={'marginTop': '15px'})
                ])
            ]
        ),

        html.Div(
            id='edit-modal',
            style={'display': 'none'},
            children=[
                html.Div(
                    id='modal-content',
                    children=[
                        html.H2('Edit Animal Data', style={'marginBottom': '20px'}),
                        html.Div(id='edit-form'),
                        html.Div(
                            style={'marginTop': '20px', 'display': 'flex', 'gap': '10px', 'justifyContent': 'flex-end'},
                            children=[
                                html.Button('Cancel', id='cancel-edit-btn', n_clicks=0, className='action-button'),
                                html.Button('Save Changes', id='save-edit-btn', n_clicks=0, className='action-button', 
                                          style={'backgroundColor': '#27ae60'})
                            ]
                        )
                    ]
                )
            ]
        ),

        html.Div(
            id='charts-section',
            style={'display': 'grid', 'gridTemplateColumns': 'repeat(auto-fit, minmax(400px, 1fr))', 'gap': '20px', 'marginBottom': '20px'},
            children=[
                html.Div(id='chart-breeds', className='card-section'),
                html.Div(id='chart-types', className='card-section')
            ]
        ),

        html.Div(
            id='map-container',
            className='card-section',
            children=[
                html.H3('Location Map', style={'marginBottom': '15px'}),
                html.Div(id='map-id')
            ]
        )
    ]
)

"""
function toggle_theme
params: n_clicks (int), theme_data (dict)
Description: Toggle between light and dark mode and update all component styles
"""
@app.callback(
    [Output('theme-store', 'data'),
     Output('theme-toggle', 'children'),
     Output('theme-toggle', 'style'),
     Output('main-container', 'style'),
     Output('header-section', 'style'),
     Output('filters-section', 'style'),
     Output('table-section', 'style'),
     Output('map-container', 'style')],
    [Input('theme-toggle', 'n_clicks')],
    [State('theme-store', 'data')]
)
def toggle_theme(n_clicks, theme_data):
    if n_clicks == 0:
        is_dark = False
    else:
        is_dark = not theme_data.get('dark', False)
    
    theme = get_theme_styles(is_dark)
    button_text = '☀️ Light Mode' if is_dark else '🌙 Dark Mode'
    
    button_style = {
        'padding': '10px 20px',
        'fontSize': '16px',
        'border': 'none',
        'borderRadius': '25px',
        'cursor': 'pointer',
        'fontWeight': 'bold',
        'boxShadow': '0 2px 4px rgba(0,0,0,0.2)',
        'transition': 'all 0.3s',
        'backgroundColor': theme['accent'],
        'color': 'white'
    }
    
    main_style = {
        'backgroundColor': theme['background'],
        'minHeight': '100vh',
        'padding': '20px',
        'color': theme['text'],
        'transition': 'all 0.3s'
    }
    
    card_style = {
        'backgroundColor': theme['card'],
        'padding': '20px',
        'marginBottom': '20px',
        'borderRadius': '10px',
        'boxShadow': '0 2px 8px rgba(0,0,0,0.1)',
        'color': theme['text']
    }
    
    header_style = {**card_style, 'padding': '30px'}
    
    return (
        {'dark': is_dark},
        button_text,
        button_style,
        main_style,
        header_style,
        card_style,
        card_style,
        card_style
    )

"""
function update_stats
params: data (list), theme_data (dict)
Description: Update statistics cards with current data counts and theme colors
"""
@app.callback(
    Output('stats-section', 'children'),
    [Input('datatable-id', 'derived_virtual_data'),
     Input('theme-store', 'data')]
)
def update_stats(data, theme_data):
    if not data:
        return []
    
    theme = get_theme_styles(theme_data.get('dark', False))
    dff = pd.DataFrame(data)
    
    stats = [
        {'label': 'Total Animals', 'value': len(dff), 'color': theme['primary']},
        {'label': 'Breeds', 'value': dff['breed'].nunique(), 'color': theme['secondary']},
        {'label': 'Types', 'value': dff['animal_type'].nunique(), 'color': theme['accent']}
    ]
    
    return html.Div(
        style={
            'display': 'grid',
            'gridTemplateColumns': 'repeat(auto-fit, minmax(200px, 1fr))',
            'gap': '20px',
            'marginBottom': '20px'
        },
        children=[
            html.Div(
                style={
                    'backgroundColor': theme['card'],
                    'padding': '20px',
                    'borderRadius': '10px',
                    'boxShadow': '0 2px 8px rgba(0,0,0,0.1)',
                    'textAlign': 'center'
                },
                children=[
                    html.H2(str(stat['value']), style={'color': stat['color'], 'margin': '0', 'fontSize': '36px'}),
                    html.P(stat['label'], style={'color': theme['text_secondary'], 'margin': '10px 0 0 0', 'fontSize': '14px'})
                ]
            ) for stat in stats
        ]
    )

"""
function update_table_styles
params: theme_data (dict)
Description: Update data table styling based on current theme
"""
@app.callback(
    [Output('datatable-id', 'style_header'),
     Output('datatable-id', 'style_data_conditional'),
     Output('datatable-id', 'style_cell')],
    [Input('theme-store', 'data')]
)
def update_table_styles(theme_data):
    theme = get_theme_styles(theme_data.get('dark', False))
    
    header_style = {
        'backgroundColor': theme['table_header'],
        'color': 'white',
        'fontWeight': 'bold'
    }
    
    data_conditional = [
        {'if': {'row_index': 'odd'}, 'backgroundColor': theme['hover'], 'color': theme['text']},
        {'if': {'row_index': 'even'}, 'backgroundColor': theme['card'], 'color': theme['text']},
        {'if': {'state': 'selected'}, 'backgroundColor': theme['table_selected'], 'color': 'white'}
    ]
    
    cell_style = {
        'textAlign': 'left',
        'padding': '12px',
        'fontSize': '13px',
        'backgroundColor': theme['card'],
        'color': theme['text']
    }
    
    return header_style, data_conditional, cell_style

"""
function toggle_edit_modal
params: edit_clicks (int), cancel_clicks (int), data (list), selected_rows (list), theme_data (dict)
Description: Show or hide edit modal and populate form with selected animal data
"""
@app.callback(
    [Output('edit-modal', 'style'),
     Output('edit-form', 'children'),
     Output('selected-animal-store', 'data')],
    [Input('edit-btn', 'n_clicks'),
     Input('cancel-edit-btn', 'n_clicks')],
    [State('datatable-id', 'derived_virtual_data'),
     State('datatable-id', 'derived_virtual_selected_rows'),
     State('theme-store', 'data')]
)
def toggle_edit_modal(edit_clicks, cancel_clicks, data, selected_rows, theme_data):
    ctx = dash.callback_context
    if not ctx.triggered:
        raise PreventUpdate
    
    button_id = ctx.triggered[0]['prop_id'].split('.')[0]
    theme = get_theme_styles(theme_data.get('dark', False))
    
    if button_id == 'cancel-edit-btn':
        return {'display': 'none'}, [], None
    
    if button_id == 'edit-btn' and edit_clicks > 0:
        if not selected_rows or not data:
            return {'display': 'none'}, [], None
        
        row_idx = selected_rows[0]
        animal_data = data[row_idx]
        
        form_fields = []
        for key, value in animal_data.items():
            form_fields.append(
                html.Div([
                    html.Label(key.replace('_', ' ').title() + ':', 
                             style={'fontWeight': 'bold', 'display': 'block', 'marginBottom': '5px', 'color': theme['text']}),
                    dcc.Input(
                        id={'type': 'edit-input', 'field': key},
                        value=str(value),
                        style={
                            'width': '100%',
                            'padding': '8px',
                            'borderRadius': '5px',
                            'border': f'1px solid {theme["border"]}',
                            'backgroundColor': theme['background'],
                            'color': theme['text']
                        }
                    )
                ], style={'marginBottom': '15px'})
            )
        
        modal_style = {
            'display': 'flex',
            'position': 'fixed',
            'top': '0',
            'left': '0',
            'width': '100%',
            'height': '100%',
            'backgroundColor': 'rgba(0,0,0,0.7)',
            'justifyContent': 'center',
            'alignItems': 'center',
            'zIndex': '1000'
        }
        
        return modal_style, form_fields, animal_data
    
    raise PreventUpdate

"""
function update_modal_style
params: theme_data (dict)
Description: Update modal content styling based on current theme
"""
@app.callback(
    Output('modal-content', 'style'),
    [Input('theme-store', 'data')]
)
def update_modal_style(theme_data):
    theme = get_theme_styles(theme_data.get('dark', False))
    return {
        'backgroundColor': theme['card'],
        'padding': '30px',
        'borderRadius': '10px',
        'maxWidth': '600px',
        'maxHeight': '80vh',
        'overflowY': 'auto',
        'width': '90%',
        'color': theme['text'],
        'boxShadow': '0 4px 20px rgba(0,0,0,0.3)'
    }

"""
function save_edit
params: n_clicks (int), values (list), ids (list), original_data (dict), current_table_data (list)
Description: Save edited animal data to MongoDB and update table
"""
@app.callback(
    [Output('datatable-id', 'data', allow_duplicate=True),
     Output('edit-modal', 'style', allow_duplicate=True)],
    [Input('save-edit-btn', 'n_clicks')],
    [State({'type': 'edit-input', 'field': dash.dependencies.ALL}, 'value'),
     State({'type': 'edit-input', 'field': dash.dependencies.ALL}, 'id'),
     State('selected-animal-store', 'data'),
     State('datatable-id', 'data')],
    prevent_initial_call=True
)
def save_edit(n_clicks, values, ids, original_data, current_table_data):
    if n_clicks == 0 or not original_data:
        raise PreventUpdate
    
    updated_data = {}
    for value, id_dict in zip(values, ids):
        field = id_dict['field']
        updated_data[field] = value
    
    try:
        query = {k: original_data[k] for k in ['animal_id'] if k in original_data}
        if not query and 'name' in original_data:
            query = {'name': original_data['name']}
        
        if query:
            result = db.update(query, updated_data)
            print(f"Updated {result.get('modified', 0)} record(s)")
    except Exception as e:
        print(f"Error updating database: {e}")
    
    new_data = current_table_data.copy()
    for i, row in enumerate(new_data):
        match = all(row.get(k) == original_data.get(k) for k in query.keys())
        if match:
            new_data[i] = updated_data
            break
    
    return new_data, {'display': 'none'}

"""
function update_dropdown_styles
params: theme_data (dict)
Description: Update dropdown styling based on current theme
"""
@app.callback(
    [Output('filter-type', 'style'),
     Output('filter-breed', 'style')],
    [Input('theme-store', 'data')]
)
def update_dropdown_styles(theme_data):
    theme = get_theme_styles(theme_data.get('dark', False))
    dropdown_style = {'backgroundColor': theme['background'], 'color': theme['text']}
    return dropdown_style, dropdown_style

"""
function update_breed_options
params: selected_types (list)
Description: Update breed dropdown options based on selected animal types
"""
@app.callback(
    Output('filter-breed', 'options'),
    [Input('filter-type', 'value')]
)
def update_breed_options(selected_types):
    if not selected_types or df.empty:
        breeds = sorted(df['breed'].dropna().unique()) if not df.empty else []
    else:
        filtered = df[df['animal_type'].isin(selected_types)]
        breeds = sorted(filtered['breed'].dropna().unique())
    return [{'label': b, 'value': b} for b in breeds]

"""
function update_table
params: animal_types (list), breeds (list), reset_clicks (int)
Description: Filter table data based on selected filters
"""
@app.callback(
    Output('datatable-id', 'data', allow_duplicate=True),
    [Input('filter-type', 'value'),
     Input('filter-breed', 'value'),
     Input('reset-btn', 'n_clicks')],
    prevent_initial_call=True
)
def update_table(animal_types, breeds, reset_clicks):
    if df.empty:
        return []
    
    filtered = df.copy()
    if animal_types:
        filtered = filtered[filtered['animal_type'].isin(animal_types)]
    if breeds:
        filtered = filtered[filtered['breed'].isin(breeds)]
    
    return filtered.to_dict('records')

"""
function update_breed_chart
params: data (list), theme_data (dict)
Description: Create bar chart showing top 10 breeds
"""
@app.callback(
    Output('chart-breeds', 'children'),
    [Input('datatable-id', 'derived_virtual_data'),
     Input('theme-store', 'data')]
)
def update_breed_chart(data, theme_data):
    if not data:
        return html.P("No data")
    
    theme = get_theme_styles(theme_data.get('dark', False))
    dff = pd.DataFrame(data)
    breed_counts = dff['breed'].value_counts().head(10)
    
    fig = px.bar(
        x=breed_counts.values,
        y=breed_counts.index,
        orientation='h',
        title='Top 10 Breeds',
        labels={'x': 'Count', 'y': 'Breed'}
    )
    
    fig.update_layout(
        height=400,
        paper_bgcolor=theme['card'],
        plot_bgcolor=theme['card'],
        font={'color': theme['text']},
        title_font_color=theme['text']
    )
    
    return dcc.Graph(figure=fig)

"""
function update_type_chart
params: data (list), theme_data (dict)
Description: Create pie chart showing animal type distribution
"""
@app.callback(
    Output('chart-types', 'children'),
    [Input('datatable-id', 'derived_virtual_data'),
     Input('theme-store', 'data')]
)
def update_type_chart(data, theme_data):
    if not data:
        return html.P("No data")
    
    theme = get_theme_styles(theme_data.get('dark', False))
    dff = pd.DataFrame(data)
    type_counts = dff['animal_type'].value_counts()
    
    fig = px.pie(
        values=type_counts.values,
        names=type_counts.index,
        title='Animal Types',
        hole=0.4
    )
    
    fig.update_layout(
        height=400,
        paper_bgcolor=theme['card'],
        plot_bgcolor=theme['card'],
        font={'color': theme['text']},
        title_font_color=theme['text']
    )
    
    return dcc.Graph(figure=fig)

"""
function update_map
params: data (list), selected_rows (list)
Description: Display map with marker at selected animal location
"""
@app.callback(
    Output('map-id', 'children'),
    [Input('datatable-id', 'derived_virtual_data'),
     Input('datatable-id', 'derived_virtual_selected_rows')]
)
def update_map(data, selected_rows):
    if not data:
        return html.P("No data available")
    
    dff = pd.DataFrame(data)
    row_idx = selected_rows[0] if selected_rows else 0
    
    try:
        lat = float(dff.iloc[row_idx]['location_lat'])
        lon = float(dff.iloc[row_idx]['location_long'])
        name = dff.iloc[row_idx].get('name', 'Unknown')
        breed = dff.iloc[row_idx].get('breed', 'Unknown')
    except:
        lat, lon = 30.75, -97.48
        name, breed = 'Unknown', 'Unknown'
    
    return dl.Map(
        style={'width': '100%', 'height': '500px'},
        center=[lat, lon],
        zoom=13,
        children=[
            dl.TileLayer(),
            dl.Marker(
                position=[lat, lon],
                children=[
                    dl.Tooltip(breed),
                    dl.Popup([html.H4(name), html.P(f"Breed: {breed}")])
                ]
            )
        ]
    )

"""
function reset_filters
params: n (int)
Description: Clear all filter selections
"""
@app.callback(
    [Output('filter-type', 'value'),
     Output('filter-breed', 'value')],
    [Input('reset-btn', 'n_clicks')]
)
def reset_filters(n):
    return None, None

if __name__ == '__main__':
    print("\n" + "="*50)
    print("Starting Dashboard Server...")
    print("Open your browser to: http://localhost:8050")
    print("Press CTRL+C to stop the server")
    print("="*50 + "\n")
    
    app.run_server(debug=True, host='localhost', port=8050)