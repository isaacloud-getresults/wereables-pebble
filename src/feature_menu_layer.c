#include "pebble.h"

/////////////////////////////////////
static Window *login_window;
static Window *beacons_window;
static Window *beacon_details_window;
static Window *games_window;
static Window *game_details_window;
static MenuLayer *login_menu_layer;
static MenuLayer *beacons_menu_layer;
static MenuLayer *games_menu_layer;
static TextLayer *login_text_layer;
static TextLayer *beacons_textbar_layer;
static TextLayer *beacon_details_textbar_layer;
static TextLayer *beacon_details_uppertext_layer;
static InverterLayer *beacons_textbar_inverter_layer;
static InverterLayer *beacon_details_textbar_inverter_layer;
static Layer *beacon_details_distance_layer;

static char *current_user_name;
static char *current_beacon_name;
static char *current_game_name;
static float current_beacon_distance;
/////////////////////////////////////

///////////////////////////////////// USERS INITIALISING
typedef struct {
    char *name;
    uint16_t id;
} User;

User users[] = {
    { .name = "ppalka", .id = 1001},
    { .name = "mwarchol", .id = 1002},
    { .name = "new user", .id = 0}
};

#define NUM_USERS sizeof(users) / sizeof(User)

static uint16_t get_num_users(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    return NUM_USERS;
}
/////////////////////////////////////

///////////////////////////////////// BEACONS INITIALISING
typedef struct {
    char *name;
    uint16_t uuid;
    bool in_range;
} Beacon;

Beacon beacons[] = {
    { .name = "Kitchen 1", .uuid = 1001, .in_range = true},
    { .name = "Kitchen 2", .uuid = 1002, .in_range = false},
    { .name = "Kitchen 3", .uuid = 1003, .in_range = false},
    { .name = "Conf. Room 1", .uuid = 1004, .in_range = false},
    { .name = "Conf. Room 2", .uuid = 1005, .in_range = true},
    { .name = "Boss Room", .uuid = 1006, .in_range = false}
};
#define NUM_BEACONS sizeof(beacons) / sizeof(Beacon)

static uint16_t get_num_beacons(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    int in_range_amount = 0;
    uint16_t i;
    for(i=0; i<NUM_BEACONS; ++i)
        if(beacons[i].in_range)
            in_range_amount++;
    if(section_index == 0)
        return in_range_amount;
    else
        return NUM_BEACONS-in_range_amount;
}
/////////////////////////////////////

///////////////////////////////////// COMMUNICATION
enum {
    REQUEST = 1,
    REQUEST_USERS = 11,
    REQUEST_BEACONS_IN_RANGE = 12,
    REQUEST_BEACONS_OUT_OF_RANGE = 13,
    REQUEST_GAMES_ACTIVE = 14,
    REQUEST_GAMES_COMPLETED = 15,
    REQUEST_LOGIN = 16,
    REQUEST_DISTANCE = 17,
    REQUEST_PROGRESS = 18,
    RESPONSE_USERS = 21,
    RESPONSE_BEACONS_IN_RANGE = 22,
    RESPONSE_BEACONS_OUT_OF_RANGE = 23,
    RESPONSE_GAMES_ACTIVE = 24,
    RESPONSE_GAMES_COMPLETED = 25,
    RESPONSE_LOGIN = 26,
    RESPONSE_DISTANCE = 27,
    RESPONSE_PROGRESS = 28
};

static void send_request(int8_t req) {
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);

    Tuplet value = TupletInteger(REQUEST,REQUEST_USERS);
    dict_write_tuplet(iter,&value);
    dict_write_end(iter);
    
    app_message_outbox_send();
}

void out_sent_handler(DictionaryIterator *sent, void *context) {
    // outgoing message was delivered
}

void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
    // outgoing message failed
}

void in_received_handler(DictionaryIterator *received, void *context) {
    // Check for fields you expect to receive
    Tuple *text_tuple = dict_find(received,RESPONSE_USERS);

    // Act on the found fields received
    if (text_tuple) {
        text_layer_set_text(login_text_layer,text_tuple->value->cstring);
    }
}

void in_dropped_handler(AppMessageResult reason, void *context) {
    // incoming message dropped
}
/////////////////////////////////////

///////////////////////////////////// LOGIN WINDOW
static void draw_user_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    User *users = (User*) callback_context;
    User *user = &users[cell_index->row];
    
    menu_cell_basic_draw(ctx, cell_layer, user->name, NULL, NULL);
}

static void user_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    current_user_name = users[cell_index->row].name;
    
    window_stack_push(beacons_window, true);
    
    send_request(REQUEST_BEACONS_IN_RANGE);
}

MenuLayerCallbacks login_menu_callbacks = {
    .get_num_rows = get_num_users,
    .draw_row = draw_user_row,
    .select_click = user_select_click
};

static void login_window_load(Window *window) {
    Layer *window_layer = window_get_root_layer(window);
    int textfield_height = 60;
    
    GRect text_bounds = layer_get_bounds(window_layer);
    text_bounds.size.h = textfield_height;
    login_text_layer = text_layer_create(text_bounds);
    text_layer_set_text(login_text_layer,"SoI Beacons\nlogin as:");
    text_layer_set_font(login_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(login_text_layer, GTextAlignmentCenter);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textfield_height;
    menu_bounds.origin.y += textfield_height;
    login_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(login_menu_layer, users, login_menu_callbacks);
    menu_layer_set_click_config_onto_window(login_menu_layer, window);
    
    layer_add_child(window_layer, text_layer_get_layer(login_text_layer));
    layer_add_child(window_layer, menu_layer_get_layer(login_menu_layer));
}

static void login_window_unload(Window *window) {
    text_layer_destroy(login_text_layer);
    menu_layer_destroy(login_menu_layer);
}
/////////////////////////////////////

///////////////////////////////////// BEACONS WINDOW
static uint16_t get_num_sections_beacons(MenuLayer *menu_layer, void *data) {
    return 2;
}

static int16_t beacons_get_header_height(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

static int16_t beacons_get_cell_height(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
    return 30;
}

static void draw_beacon_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    switch (section_index) {
        case 0:
            menu_cell_basic_header_draw(ctx, cell_layer, "Beacons in range");
            break;
            
        case 1:
            menu_cell_basic_header_draw(ctx, cell_layer, "Beacons out of range");
            break;
    }
}

static void draw_beacon_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    Beacon *beacons = (Beacon*) callback_context;
    Beacon *beacon;
    int i = -1, j;
    
    switch (cell_index->section) {
        case 0:
            for(j=0; i!=cell_index->row; ++j)
                if(beacons[j].in_range)
                    i++;
            beacon = &beacons[j-1];
            menu_cell_basic_draw(ctx, cell_layer, beacon->name, NULL, NULL);
            break;
        case 1:
            for(j=0; i!=cell_index->row; ++j)
                if(!(beacons[j].in_range))
                    i++;
            beacon = &beacons[j-1];
            menu_cell_basic_draw(ctx, cell_layer, beacon->name, NULL, NULL);
            break;
    }
}

static void beacon_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    int i = -1, j = 0;
    if(cell_index->section==0) {
        for(j=0; i!=cell_index->row; ++j)
            if(beacons[j].in_range)
                i++;
    }
    else if(cell_index->section==1) {
        for(j=0; i!=cell_index->row; ++j)
            if(!(beacons[j].in_range))
                i++;
    }
    current_beacon_name = beacons[j-1].name;
    
    window_stack_push(beacon_details_window, true);
}

MenuLayerCallbacks beacons_menu_callbacks = {
    .get_num_sections = get_num_sections_beacons,
    .get_num_rows = get_num_beacons,
    .get_header_height = beacons_get_header_height,
    .get_cell_height = beacons_get_cell_height, 
    .draw_header = draw_beacon_header,
    .draw_row = draw_beacon_row,
    .select_click = beacon_select_click
};

static void beacons_window_load(Window *window) {
    Layer *window_layer = window_get_root_layer(window);
    int textbar_height = 18;
    
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    beacons_textbar_layer = text_layer_create(textbar_bounds);
    static char text_buffer[30];
    snprintf(text_buffer,30,"%s",current_user_name);
    text_layer_set_text(beacons_textbar_layer,text_buffer);
    text_layer_set_font(beacons_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_14));
    text_layer_set_text_alignment(beacons_textbar_layer, GTextAlignmentCenter);
    
    beacons_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    beacons_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(beacons_menu_layer, beacons, beacons_menu_callbacks);
    menu_layer_set_click_config_onto_window(beacons_menu_layer, window);

    layer_add_child(window_layer, text_layer_get_layer(beacons_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(beacons_textbar_inverter_layer));
    layer_add_child(window_layer, menu_layer_get_layer(beacons_menu_layer));
}

static void beacons_window_unload(Window *window) {
    text_layer_destroy(beacons_textbar_layer);
    inverter_layer_destroy(beacons_textbar_inverter_layer);
    menu_layer_destroy(beacons_menu_layer);
}
/////////////////////////////////////

///////////////////////////////////// BEACON DETAILS WINDOW
static void dinstance_layer_update(Layer *layer, GContext *ctx) {
    int height = 34;
    int width = 136;
    graphics_context_set_stroke_color(ctx, GColorBlack);
    graphics_draw_round_rect(ctx,GRect(4,3,width,height),8);
    graphics_context_set_fill_color(ctx, GColorBlack);
    graphics_fill_rect(ctx,GRect(4,3,width*current_beacon_distance,height),8,(GCornerTopLeft|GCornerBottomLeft));
}

static void beacon_details_window_load(Window *window) {
    Layer *window_layer = window_get_root_layer(window);
    int textbar_height = 18;
    int uppertext_layer_height = 30;
    int distance_layer_height = 40;
        
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    beacon_details_textbar_layer = text_layer_create(textbar_bounds);
    static char text_buffer[30];
    snprintf(text_buffer,30,"%s",current_beacon_name);
    text_layer_set_text(beacon_details_textbar_layer,text_buffer);
    text_layer_set_font(beacon_details_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_14));
    text_layer_set_text_alignment(beacon_details_textbar_layer, GTextAlignmentCenter);
    
    beacon_details_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect uppertext_bounds = layer_get_bounds(window_layer);
    uppertext_bounds.size.h = uppertext_layer_height;
    uppertext_bounds.origin.y = textbar_height;
    beacon_details_uppertext_layer = text_layer_create(uppertext_bounds);
    text_layer_set_text(beacon_details_uppertext_layer,"Distance:");
    text_layer_set_font(beacon_details_uppertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(beacon_details_uppertext_layer, GTextAlignmentCenter);
    
    GRect distance_layer_bounds = layer_get_bounds(window_layer);
    distance_layer_bounds.size.h = distance_layer_height;
    distance_layer_bounds.origin.y = textbar_height + uppertext_layer_height;
    beacon_details_distance_layer = layer_create(distance_layer_bounds);
    layer_set_update_proc(beacon_details_distance_layer, dinstance_layer_update);
    
    layer_add_child(window_layer, text_layer_get_layer(beacon_details_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(beacon_details_textbar_inverter_layer));
    layer_add_child(window_layer, text_layer_get_layer(beacon_details_uppertext_layer));
    layer_add_child(window_layer, beacon_details_distance_layer);
}

static void beacon_details_window_unload(Window *window) {
    text_layer_destroy(beacon_details_textbar_layer);
    inverter_layer_destroy(beacon_details_textbar_inverter_layer);
    text_layer_destroy(beacon_details_uppertext_layer);
    layer_destroy(beacon_details_distance_layer);
}
/////////////////////////////////////

/////////////////////////////////////
static WindowHandlers login_window_handlers = {
    .load = login_window_load,
    .unload = login_window_unload
};
static WindowHandlers beacons_window_handlers = {
    .load = beacons_window_load,
    .unload = beacons_window_unload
};
static WindowHandlers beacon_details_window_handlers = {
    .load = beacon_details_window_load,
    .unload = beacon_details_window_unload
};
static WindowHandlers games_window_handlers = {
    .load = NULL,
    .unload = NULL
};
static WindowHandlers game_details_window_handlers = {
    .load = NULL,
    .unload = NULL
};
/////////////////////////////////////

/////////////////////////////////////
static void init() {
    current_user_name = "NO USER";
    current_beacon_name = "NO BEACON";
    current_game_name = "NO GAME";
    current_beacon_distance = 0.4;
    
    login_window = window_create();
    window_set_fullscreen(login_window, true);
    window_set_window_handlers(login_window, login_window_handlers);
    
    beacons_window = window_create();
    window_set_fullscreen(beacons_window, true);
    window_set_window_handlers(beacons_window, beacons_window_handlers);
    
    beacon_details_window = window_create();
    window_set_fullscreen(beacon_details_window, true);
    window_set_window_handlers(beacon_details_window, beacon_details_window_handlers);
    
    games_window = window_create();
    window_set_fullscreen(games_window, true);
    window_set_window_handlers(games_window, games_window_handlers);
    
    game_details_window = window_create();
    window_set_fullscreen(game_details_window, true);
    window_set_window_handlers(game_details_window, game_details_window_handlers);
    
    app_message_register_inbox_received(in_received_handler);
    app_message_register_inbox_dropped(in_dropped_handler);
    app_message_register_outbox_sent(out_sent_handler);
    app_message_register_outbox_failed(out_failed_handler);
    
    const int inbound_size = 64;
    const int outbound_size = 64;
    app_message_open(inbound_size, outbound_size);
    
    window_stack_push(login_window, true);
    
    send_request(REQUEST_USERS);
}

static void deinit() {
    window_destroy(login_window);
    window_destroy(beacons_window);
    window_destroy(beacon_details_window);
    window_destroy(games_window);
    window_destroy(game_details_window);
}
/////////////////////////////////////

int main(void) {
    init();
    app_event_loop();
    deinit();
}
