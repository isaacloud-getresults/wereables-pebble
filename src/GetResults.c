///    @author: Pawel Palka
///    @email: ppalka@sosoftware.pl    // company
///    @email: fractalord@gmail.com    // private

#include "pebble.h"

static Window *login_window = NULL;
static Window *user_window = NULL;
static Window *beacons_window = NULL;
static Window *beacon_details_window = NULL;
static Window *games_window = NULL;
static Window *game_details_window = NULL;
static Window *achievement_details_window = NULL;
static SimpleMenuLayer *login_menu_layer = NULL;
static MenuLayer *achievements_menu_layer = NULL;
static MenuLayer *beacons_menu_layer = NULL;
static MenuLayer *games_menu_layer = NULL;
static TextLayer *login_uppertext_layer = NULL;
static TextLayer *login_lowertext_layer = NULL;
static TextLayer *user_textbar_layer = NULL;
static TextLayer *user_text_layer = NULL;
static TextLayer *beacons_textbar_layer = NULL;
static TextLayer *beacon_details_textbar_layer = NULL;
static TextLayer *beacon_details_uppertext_layer = NULL;
static TextLayer *beacon_details_lowertext_layer = NULL;
static TextLayer *games_textbar_layer = NULL;
static TextLayer *game_details_textbar_layer = NULL;
static TextLayer *game_details_text_layer = NULL;
static TextLayer *achievement_details_textbar_layer = NULL;
static TextLayer *achievement_details_text_layer = NULL;
static InverterLayer *user_textbar_inverter_layer = NULL;
static InverterLayer *beacons_textbar_inverter_layer = NULL;
static InverterLayer *beacon_details_textbar_inverter_layer = NULL;
static InverterLayer *games_textbar_inverter_layer = NULL;
static InverterLayer *game_details_textbar_inverter_layer = NULL;
static InverterLayer *achievement_details_textbar_inverter_layer = NULL;
static Layer *user_downloading_sign_layer = NULL;
static Layer *beacon_details_distance_layer = NULL;
static Layer *beacons_downloading_sign_layer = NULL;
static AppTimer *timer = NULL;

typedef struct {
    char *name;
    int points;
    int rank;
    int beacons;
    int achievements;
} User;

typedef struct {
    char *name;
    int distance;
    int games_active;
    int games_completed;
} Beacon;

typedef struct {
    char *name;
    char *description;
    int progress;
    int goal;
} Game;

typedef struct {
    char *name;
    char *description;
} Achievement;

static Beacon *current_beacon = NULL;
static Game *current_game = NULL;
static Achievement *current_achievement = NULL;

static User user;
static Beacon **beacons = NULL;
static Game **games = NULL;
static Achievement **achievements = NULL;

uint16_t num_beacons;
uint16_t num_beacons_in_range;
uint16_t num_beacons_out_of_range;
uint16_t num_games;
uint16_t num_games_active;
uint16_t num_games_completed;
uint16_t num_achievements;
uint16_t prev_num_achievements;

static bool is_downloading;

static int textbar_height = 24;

static uint16_t get_num_beacons(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_beacons() start");
    int i;
    num_beacons_in_range = num_beacons_out_of_range = 0;
    for(i=0; i<num_beacons; ++i) {
        if(beacons[i]->distance>0)
            num_beacons_in_range++;
        else
            num_beacons_out_of_range++;
    }
    uint16_t num = 0;
    if(section_index==0)
        num = num_beacons_in_range;
    else if(section_index==1)
        num = num_beacons_out_of_range;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_beacons() end, return: %u",num);
    return num;
}

static uint16_t get_num_games(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_beacons() start");
    int i;
    num_games_active = num_games_completed = 0;
    for(i=0; i<num_games; ++i) {
        if(games[i]->progress==games[i]->goal)
            num_games_completed++;
        else
            num_games_active++;
    }
    uint16_t num = 0;
    if(section_index==0)
        num = num_games_active;
    else if(section_index==1)
        num = num_games_completed;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_games() end, return: %u",num);
    return num;
}

static uint16_t get_num_achievements(struct MenuLayer* menu_layer, uint16_t section_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_achievements() start");
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_achievements() end, return: %u",num_achievements);
    return num_achievements;
}

static int beacons_compare(const void *b1, const void *b2) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_compare()");
    // beacons closer to user (distance value higher) are placed in the table first
    int d1 = (*(Beacon**)b1)->distance;
    int d2 = (*(Beacon**)b2)->distance;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "    d1: %i | d2: %i",d1,d2);
    return d2-d1;
}

static int games_compare(const void *g1, const void *g2) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "games_compare()");
    // games with bigger progress/goal ratio are placed in the table first,
    // then after all active are placed inactive unsorted
    // (that means the same order in which they were recieved)
    double p1 = (double)((*(Game**)g1)->progress)/(double)((*(Game**)g1)->goal);
    double p2 = (double)((*(Game**)g2)->progress)/(double)((*(Game**)g2)->goal);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "    p1=%f | p2=%f",p1,p2);
    return (int)((p2-p1)*1000);
}

static bool update_beacons_table(Beacon *new_beacon) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_beacons_table() new_beacon->name: %s start",new_beacon->name);
    if(beacons==NULL) {
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    table empty, allocating new table");
        beacons = (Beacon**)calloc(user.beacons,sizeof(Beacon*));
        char *new_name = (char*)calloc(strlen(new_beacon->name),sizeof(char));
        strcpy(new_name,new_beacon->name);
        beacons[0] = new_beacon;
        beacons[0]->name = new_name;
        num_beacons = 0;
        num_beacons++;
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    added first beacon: %s",beacons[0]->name);
        return true;
    }
    else {
        int size = num_beacons;
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "    table_size: %u",size);
        int i;
        for(i=0; i<size && i<user.beacons; ++i) {
            // if that beacon already exists in the table
            if(strcmp(beacons[i]->name,new_beacon->name)==0) {
                // overwrite games counters (probably faster than checking them and overwriting if changed)
                beacons[i]->games_active=new_beacon->games_active;
                beacons[i]->games_completed=new_beacon->games_completed;
                // if distance changed
                if(beacons[i]->distance!=new_beacon->distance) {
                    APP_LOG(APP_LOG_LEVEL_DEBUG, "    beacon found, distance changed");
                    beacons[i]->distance = new_beacon->distance;
                    free(new_beacon);
                    qsort(beacons,size,sizeof(Beacon*),beacons_compare);
                    return true;
                }
                else {
                    APP_LOG(APP_LOG_LEVEL_DEBUG, "    beacon found, distance not changed, rejecting");
                    free(new_beacon);
                    return false;
                }
            }
        }
        // add new if not found in table
        APP_LOG(APP_LOG_LEVEL_DEBUG, "    beacon not found, adding new");
        char *new_name = (char*)calloc(strlen(new_beacon->name),sizeof(char));
        strcpy(new_name,new_beacon->name);
        beacons[size] = new_beacon;
        beacons[size]->name = new_name;
        num_beacons++;
        qsort(beacons,size+1,sizeof(Beacon*),beacons_compare);
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "update_beacons_table() end");
        return true;
    }
}

static bool update_games_table(Game *new_game) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "update_games_table()");
    if(games==NULL) {
        APP_LOG(APP_LOG_LEVEL_DEBUG, "    table empty, allocating new table");
        games = (Game**)calloc(current_beacon->games_active+current_beacon->games_completed,sizeof(Game*));
        games[0] = new_game;
        num_games = 1;
        APP_LOG(APP_LOG_LEVEL_DEBUG, "    added first game: %s",games[0]->name);
        return true;
    }
    else {
        int size = num_games_active + num_games_completed;
        APP_LOG(APP_LOG_LEVEL_DEBUG, "    table_size: %u",size);
        int i;
        for(i=0; i<size && i<(current_beacon->games_active+current_beacon->games_completed); ++i) {
            // if that game already exists in the table
            if(strcmp(games[i]->name,new_game->name)==0) {
                // we assume that description and goal of the game don't change
                if(games[i]->progress!=new_game->progress) {
                    APP_LOG(APP_LOG_LEVEL_DEBUG, "    game found, progress changed");
                    games[i]->progress = new_game->progress;
                    free(new_game);
                    qsort(games,size,sizeof(Game*),games_compare);
                    return true;
                }
                else {
                    APP_LOG(APP_LOG_LEVEL_DEBUG, "    game found, progress not changed, rejecting");
                    free(new_game);
                    return false;
                }
            }
        }
        // add new if not found in table
        APP_LOG(APP_LOG_LEVEL_DEBUG, "    game not found, adding new");
        games[size] = new_game;
        num_games++;
        qsort(games,size+1,sizeof(Game*),games_compare);
        APP_LOG(APP_LOG_LEVEL_DEBUG, "update_games_table() end");
        return true;
    }
}

static bool update_achievements_table(Achievement *new_achievement) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "update_achievements_table()");
    if(achievements==NULL) {
        APP_LOG(APP_LOG_LEVEL_DEBUG, "    table empty, allocating new table");
        achievements = (Achievement**)calloc(user.achievements,sizeof(Achievement*));
        char *new_name = (char*)calloc(strlen(new_achievement->name),sizeof(char));
        strcpy(new_name,new_achievement->name);
        char *new_description = (char*)calloc(strlen(new_achievement->description),sizeof(char));
        strcpy(new_description,new_achievement->description);
        achievements[0] = new_achievement;
        achievements[0]->name = new_name;
        achievements[0]->description = new_description;
        num_achievements = 1;
        APP_LOG(APP_LOG_LEVEL_DEBUG, "    added first achievement: %s",achievements[0]->name);
        return true;
    }
    else {
        int size = num_achievements;
        APP_LOG(APP_LOG_LEVEL_DEBUG, "    table_size: %u",size);
        int i;
        for(i=0; i<size && i<user.achievements; ++i) {
            // if that achievement already exists in the table
            if(strcmp(achievements[i]->name,new_achievement->name)==0) {
                APP_LOG(APP_LOG_LEVEL_DEBUG, "    achievement found, rejecting");
                // we assume that description of the achievement doesn't change
                //free(new_achievement->name);
                //free(new_achievement->description);
                free(new_achievement);
                return false;
            }
        }
        // add new if not found in table
        APP_LOG(APP_LOG_LEVEL_DEBUG, "    achievement not found, adding new");
        char *new_name = (char*)calloc(strlen(new_achievement->name),sizeof(char));
        strcpy(new_name,new_achievement->name);
        char *new_description = (char*)calloc(strlen(new_achievement->description),sizeof(char));
        strcpy(new_description,new_achievement->description);
        achievements[size] = new_achievement;
        achievements[size]->name = new_name;
        achievements[size]->description = new_description;
        num_achievements++;
        APP_LOG(APP_LOG_LEVEL_DEBUG, "update_achievements_table() end");
        return true;
    }
}

static void clear_beacons_table(Beacon **table) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_beacons_table() start");
    if(table!=NULL) {
        int size = num_beacons;
        int i;
        for(i=0; i<size; ++i) {
            free(table[i]->name);
            free(table[i]);
        }
        free(table);
    }
    num_beacons = 0;
    num_beacons_in_range = 0;
    num_beacons_out_of_range = 0;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_beacons_table() end");
}

static void clear_games_table(Game **table) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_games_table() start");
    if(table!=NULL) {
        APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_games_table() 1");
        int size = num_games;
        int i;
        APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_games_table() size: %i",size);
        for(i=0; i<size; ++i) {
            APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_games_table() i: %i",i);
            APP_LOG(APP_LOG_LEVEL_DEBUG, "    clearing name: %s",table[i]->name);
            free(table[i]->name);
            APP_LOG(APP_LOG_LEVEL_DEBUG, "    clearing description: %s",table[i]->description);
            free(table[i]->description);
            APP_LOG(APP_LOG_LEVEL_DEBUG, "    deleting whole game");
            if(table[i]!=NULL)
                free(table[i]);
            APP_LOG(APP_LOG_LEVEL_DEBUG, "    game deleted");
        }
        APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_games_table() 2");
        free(table);
        APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_games_table() 3");
    }
    num_games = 0;
    num_games_active = 0;
    num_games_completed = 0;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_games_table() end");
}

static void clear_achievements_table(Achievement **table) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_achievements_table() start");
    if(table!=NULL) {
        int size = num_achievements;
        int i;
        //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_achievements_table() size: %i",size);
        for(i=0; i<size && i<user.achievements; ++i) {
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_achievements_table() i: %i",i);
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "    clearing name: %s",table[i]->name);
            free(table[i]->name);
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "    clearing description: %s",table[i]->description);
            free(table[i]->description);
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "    deleting whole achievement");
            free(table[i]);
            //APP_LOG(APP_LOG_LEVEL_DEBUG, "    achievement deleted");
        }
        free(table);
    }
    num_achievements = 0;
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "clear_achievements_table() end");
}

enum {
    // request keys
    REQUEST_TYPE = 1,
    REQUEST_QUERY = 2,
    
    // request values
    REQUEST_USER = 1,
    REQUEST_BEACONS = 2,
    REQUEST_GAMES = 3,
    REQUEST_ACHIEVEMENTS = 4,
    
    // response keys
    RESPONSE_TYPE = 1,
    USER_NAME = 2,
    USER_POINTS = 3,
    USER_RANK = 4,
    USER_BEACONS = 5,
    USER_ACHIEVEMENTS = 6,
    BEACON_NAME = 2,
    BEACON_DISTANCE = 3,
    BEACON_GAMES_ACTIVE = 4,
    BEACON_GAMES_COMPLETED = 5,
    GAME_NAME = 2,
    GAME_DESCRIPTION = 3,
    GAME_PROGRESS = 4,
    GAME_GOAL = 5,
    ACHIEVEMENT_NAME = 2,
    ACHIEVEMENT_DESCRIPTION = 3,
    
    // response values
    RESPONSE_USER = 1,
    RESPONSE_BEACON = 2,
    RESPONSE_GAME = 3,
    RESPONSE_ACHIEVEMENT = 4,
    
    // wainting for variants
    WAITING_FOR_USER_DETAILS = 1,
    WAITING_FOR_BEACONS = 2,
    WAITING_FOR_BEACON_DETAILS = 3,
    WAITING_FOR_GAMES = 4,
    WAITING_FOR_GAME_DETAILS = 5,
    WAITING_FOR_ACHIEVEMENT_DETAILS = 6
};

///////////////////////////////////// COMMUNICATION
char * translate_result(AppMessageResult result) {
    switch (result) {
        case APP_MSG_OK: return "APP_MSG_OK";
        case APP_MSG_SEND_TIMEOUT: return "APP_MSG_SEND_TIMEOUT";
        case APP_MSG_SEND_REJECTED: return "APP_MSG_SEND_REJECTED";
        case APP_MSG_NOT_CONNECTED: return "APP_MSG_NOT_CONNECTED";
        case APP_MSG_APP_NOT_RUNNING: return "APP_MSG_APP_NOT_RUNNING";
        case APP_MSG_INVALID_ARGS: return "APP_MSG_INVALID_ARGS";
        case APP_MSG_BUSY: return "APP_MSG_BUSY";
        case APP_MSG_BUFFER_OVERFLOW: return "APP_MSG_BUFFER_OVERFLOW";
        case APP_MSG_ALREADY_RELEASED: return "APP_MSG_ALREADY_RELEASED";
        case APP_MSG_CALLBACK_ALREADY_REGISTERED: return "APP_MSG_CALLBACK_ALREADY_REGISTERED";
        case APP_MSG_CALLBACK_NOT_REGISTERED: return "APP_MSG_CALLBACK_NOT_REGISTERED";
        case APP_MSG_OUT_OF_MEMORY: return "APP_MSG_OUT_OF_MEMORY";
        case APP_MSG_CLOSED: return "APP_MSG_CLOSED";
        case APP_MSG_INTERNAL_ERROR: return "APP_MSG_INTERNAL_ERROR";
        default: return "UNKNOWN ERROR";
    }
}

static void send_simple_request(int8_t request) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "send_simple_request() Request: %u start",request);
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    
    Tuplet value = TupletInteger(REQUEST_TYPE,request);
    dict_write_tuplet(iter,&value);
    dict_write_end(iter);
    
    app_message_outbox_send();
    APP_LOG(APP_LOG_LEVEL_DEBUG, "send_simple_request() Request: %u end",request);
}

static void send_query_request(int8_t request, char *query) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "send_query_request() Request: %u Query: %s start",request,query);
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
    
    Tuplet value1 = TupletInteger(REQUEST_TYPE,request);
    Tuplet value2 = TupletCString(REQUEST_QUERY,query);
    dict_write_tuplet(iter,&value1);
    dict_write_tuplet(iter,&value2);
    dict_write_end(iter);
    
    app_message_outbox_send();
    APP_LOG(APP_LOG_LEVEL_DEBUG, "send_query_request() Request: %u Query: %s end",request,query);
}

void out_sent_handler(DictionaryIterator *sent, void *context) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "out_sent_handler()");
}

void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "out_failed_handler() Reason: %s",translate_result(reason));
}

void in_received_handler(DictionaryIterator *iter, void *context) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "in_received_handler() start");
    Tuple *receiving_type = dict_find(iter,RESPONSE_TYPE);
    if(receiving_type) {
        APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving_type: %u",receiving_type->value->data[0]);
        if(receiving_type->value->data[0]==RESPONSE_USER) {
            APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving user");
            Tuple *name = dict_find(iter,USER_NAME);
            Tuple *points = dict_find(iter,USER_POINTS);
            Tuple *rank = dict_find(iter,USER_RANK);
            Tuple *beacons = dict_find(iter,USER_BEACONS);
            Tuple *achievements = dict_find(iter,USER_ACHIEVEMENTS);
            if(achievements && beacons && rank && points && name) {
                if(user.name==NULL) {
                    char *new_name = (char*)calloc(strlen(name->value->cstring),sizeof(char));
                    strcpy(new_name,name->value->cstring);
                    user.name = new_name;
                }
                else {
                    if(strcmp(user.name,name->value->cstring)!=0) {
                        char *new_name = (char*)calloc(strlen(name->value->cstring),sizeof(char));
                        strcpy(new_name,name->value->cstring);
                        free(user.name);
                        user.name = new_name;
                    }
                }
                user.points = points->value->data[0]+points->value->data[1]*256;
                user.rank = rank->value->data[0]+rank->value->data[1]*256;
                user.beacons = beacons->value->data[0];
                user.achievements = achievements->value->data[0];
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved user: %s | points: %u | rank: %u | beacons: %u | achievements: %u",user.name,user.points,user.rank,user.beacons,user.achievements);
                static char text_buffer[40];
                snprintf(text_buffer,40,"%s",user.name);
                text_layer_set_text(login_lowertext_layer,text_buffer);
                layer_set_hidden(simple_menu_layer_get_layer(login_menu_layer),false);
            }
            else {
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect user dictionary");
            }
        }
        else if(receiving_type->value->data[0]==RESPONSE_BEACON) {
            APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving beacon");
            Tuple *name = dict_find(iter,BEACON_NAME);
            Tuple *distance = dict_find(iter,BEACON_DISTANCE);
            Tuple *games_active = dict_find(iter,BEACON_GAMES_ACTIVE);
            Tuple *games_completed = dict_find(iter,BEACON_GAMES_COMPLETED);
            Beacon *new_beacon = (Beacon*)malloc(sizeof(Beacon));
            if(new_beacon && games_completed && games_active && distance && name) {
                APP_LOG(APP_LOG_LEVEL_DEBUG, "strlen(name->value->cstring): %u",strlen(name->value->cstring));
                char new_name[strlen(name->value->cstring)+1];// = (char*)calloc(strlen(name->value->cstring),sizeof(char));
                strncpy(new_name,name->value->cstring,strlen(name->value->cstring)+1);
                new_beacon->name = new_name;
                new_beacon->distance = distance->value->data[0];
                new_beacon->games_active = games_active->value->data[0]+games_active->value->data[1]*256;
                new_beacon->games_completed = games_completed->value->data[0]+games_completed->value->data[1]*256;
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieving beacon: %s | distance: %u | games active: %u | games completed: %u",new_beacon->name,new_beacon->distance,new_beacon->games_active,new_beacon->games_completed);
                if(update_beacons_table(new_beacon)) {
                    if(window_stack_get_top_window()==beacons_window) {
                        APP_LOG(APP_LOG_LEVEL_DEBUG, "Reloading beacons_menu_layer");
                        menu_layer_reload_data(beacons_menu_layer);
                    }
                }
            }
            else {
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect beacon dictionary");
            }
            if(num_beacons==user.beacons) {
                is_downloading = false;
                layer_mark_dirty(beacons_downloading_sign_layer);
            }
        }
        else if(receiving_type->value->data[0]==RESPONSE_GAME) {
            APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving game");
            Tuple *name = dict_find(iter,GAME_NAME);
            Tuple *description = dict_find(iter,GAME_DESCRIPTION);
            Tuple *progress = dict_find(iter,GAME_PROGRESS);
            Tuple *goal = dict_find(iter,GAME_GOAL);
            Game *new_game = (Game*)malloc(sizeof(Game));
            if(new_game && goal && progress && description && name) {
                char *new_name = (char*)calloc(strlen(name->value->cstring),sizeof(char));
                strcpy(new_name,name->value->cstring);
                char *new_description = (char*)calloc(strlen(description->value->cstring),sizeof(char));
                strcpy(new_description,description->value->cstring);
                new_game->name = new_name;
                new_game->description = new_description;
                new_game->progress = progress->value->data[0]+progress->value->data[1]*256;
                new_game->goal = goal->value->data[0]+goal->value->data[1]*256;
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved game: %s | progress: %i/%i",new_game->name,new_game->progress,new_game->goal);
                if(update_games_table(new_game))
                    menu_layer_reload_data(games_menu_layer);
            }
            else {
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect game dictionary");
            }
            if(num_games==(current_beacon->games_active+current_beacon->games_completed))
                is_downloading = false;
        }
        else if(receiving_type->value->data[0]==RESPONSE_ACHIEVEMENT) {
            APP_LOG(APP_LOG_LEVEL_DEBUG, "Starting receiving achievement");
            Tuple *name = dict_find(iter,ACHIEVEMENT_NAME);
            Tuple *description = dict_find(iter,ACHIEVEMENT_DESCRIPTION);
            Achievement *new_achievement = (Achievement*)malloc(sizeof(Achievement));
            if(new_achievement && description && name!=NULL) {
                char new_name[strlen(name->value->cstring)+1]; // = malloc(strlen(name->value->cstring)*sizeof(char));
                strncpy(new_name,name->value->cstring,strlen(name->value->cstring)+1);
                char new_description[strlen(description->value->cstring)+1]; // = (char*)malloc(strlen(description->value->cstring)*sizeof(char));
                strncpy(new_description,description->value->cstring,strlen(description->value->cstring)+1);
                new_achievement->name = new_name;
                new_achievement->description = new_description;
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Recieved an achievement: %s",new_achievement->name);
                if(update_achievements_table(new_achievement)) {
                    if(window_stack_get_top_window()==user_window) {
                        APP_LOG(APP_LOG_LEVEL_DEBUG, "Reloading achievements_menu_layer");
                        menu_layer_reload_data(achievements_menu_layer);
                    }
                }
            }
            else {
                APP_LOG(APP_LOG_LEVEL_DEBUG, "Incorrect achievement dictionary");
            }
            if(num_achievements==user.achievements) {
                is_downloading = false;
                layer_mark_dirty(user_downloading_sign_layer);
            }
        }
    }
    else {
        APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving error, RESPONSE_TYPE tuple not found");
    }
    APP_LOG(APP_LOG_LEVEL_DEBUG, "in_received_handler() end");
}

void in_dropped_handler(AppMessageResult reason, void *context) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Receiving rejected. Reason: %s",translate_result(reason));
}
/////////////////////////////////////

///////////////////////////////////// LOGIN WINDOW
static SimpleMenuSection login_menu_sections[1];
static SimpleMenuItem login_menu_first_section_items[2];

static void login_menu_beacons_callback(int index, void *ctx) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "login_menu_beacons_callback()");
    send_simple_request(REQUEST_BEACONS);
    is_downloading = true;
    window_stack_push(beacons_window, true);
}

static void login_menu_user_callback(int index, void *ctx) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "login_menu_user_callback()");
    if(prev_num_achievements!=user.achievements) {
        clear_achievements_table(achievements);
        send_simple_request(REQUEST_ACHIEVEMENTS);
        is_downloading = true;
    }
    window_stack_push(user_window, true);
}

static void login_request_sending(void *data) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "login_request_sending()");
    if(user.name==NULL) {
        send_simple_request(REQUEST_USER);
        timer = app_timer_register(2000,login_request_sending,NULL);
    }
}

static void login_window_load(Window *window) {
    timer = app_timer_register(2000,login_request_sending,NULL);
    send_simple_request(REQUEST_USER);
    
    APP_LOG(APP_LOG_LEVEL_DEBUG, "login_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    int uppertext_height = 40;
    int lowertext_height = 39;
    
    GRect uppertext_bounds = layer_get_bounds(window_layer);
    uppertext_bounds.size.h = uppertext_height;
    login_uppertext_layer = text_layer_create(uppertext_bounds);
    text_layer_set_text(login_uppertext_layer,"Get Results!");
    text_layer_set_font(login_uppertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_28_BOLD));
    text_layer_set_text_alignment(login_uppertext_layer, GTextAlignmentCenter);
    
    GRect lowertext_bounds = layer_get_bounds(window_layer);
    lowertext_bounds.size.h = lowertext_height;
    lowertext_bounds.origin.y += uppertext_height;
    login_lowertext_layer = text_layer_create(lowertext_bounds);
    text_layer_set_text(login_lowertext_layer,"connecting...");
    text_layer_set_font(login_lowertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24));
    text_layer_set_text_alignment(login_lowertext_layer, GTextAlignmentCenter);
    
    login_menu_first_section_items[0] = (SimpleMenuItem) {
        .title = "Beacons list",
        .callback = login_menu_beacons_callback
    };
    login_menu_first_section_items[1] = (SimpleMenuItem) {
        .title = "User details",
        .callback = login_menu_user_callback
    };
    
    login_menu_sections[0] = (SimpleMenuSection) {
        .num_items = 2,
        .items = login_menu_first_section_items
    };
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= uppertext_height + lowertext_height;
    menu_bounds.origin.y += uppertext_height + lowertext_height;
    login_menu_layer = simple_menu_layer_create(menu_bounds,window,login_menu_sections,1,NULL);
    
    layer_add_child(window_layer, text_layer_get_layer(login_uppertext_layer));
    layer_add_child(window_layer, text_layer_get_layer(login_lowertext_layer));
    layer_add_child(window_layer, simple_menu_layer_get_layer(login_menu_layer));
    
    if(user.name==NULL)
        layer_set_hidden(simple_menu_layer_get_layer(login_menu_layer),true);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "login_window_load() end");
}

static void login_window_unload(Window *window) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "login_window_unload() start");
    simple_menu_layer_destroy(login_menu_layer);
    text_layer_destroy(login_lowertext_layer);
    text_layer_destroy(login_uppertext_layer);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "login_window_unload() end");
}

static void login_window_appear(Window *window) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "login_window_appear() start");
    send_simple_request(REQUEST_USER);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "login_window_appear() end");
}
/////////////////////////////////////

///////////////////////////////////// USER WINDOW
static void downloading_sign_layer_update(Layer *layer, GContext *ctx) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "downloading_sign_layer_update() start");
    if(is_downloading) {
        graphics_context_set_stroke_color(ctx, GColorWhite);
        graphics_context_set_fill_color(ctx, GColorWhite);
        graphics_fill_circle (ctx,GPoint(textbar_height/2,textbar_height/2),textbar_height/4);
    }
    APP_LOG(APP_LOG_LEVEL_DEBUG, "downloading_sign_layer_update() end");
}

static uint16_t get_num_sections_achievements(MenuLayer *menu_layer, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_sections_achievements()");
    return 1;
}

static int16_t get_header_height_achievements(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_header_height_achievements()");
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

static int16_t get_cell_height_achievements(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_cell_height_achievements()");
    return 26;
}

static void draw_achievement_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "draw_achievement_header()");
    switch (section_index) {
        case 0:
            menu_cell_basic_header_draw(ctx, cell_layer, "Achievements");
            break;
    }
}

static void draw_achievement_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "draw_achievement_row()");
    switch (cell_index->section) {
        case 0:
            if(achievements!=NULL)
                menu_cell_basic_draw(ctx, cell_layer, achievements[cell_index->row]->name, NULL, NULL);
            break;
    }
}

static void achievement_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_select_click()");
    if(cell_index->section==0 && achievements!=NULL)
        current_achievement = achievements[cell_index->row];
    
    window_stack_push(achievement_details_window, true);
}

MenuLayerCallbacks achievements_menu_callbacks = {
    .get_num_sections = get_num_sections_achievements,
    .get_num_rows = get_num_achievements,
    .get_header_height = get_header_height_achievements,
    .get_cell_height = get_cell_height_achievements,
    .draw_header = draw_achievement_header,
    .draw_row = draw_achievement_row,
    .select_click = achievement_select_click
};

static void user_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_window_load() start");
    prev_num_achievements = user.achievements;
    
    Layer *window_layer = window_get_root_layer(window);
    int text_layer_height = 52;
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    user_textbar_layer = text_layer_create(textbar_bounds);
    static char text_buffer1[30];
    snprintf(text_buffer1,30,"%s",user.name);
    text_layer_set_text(user_textbar_layer,text_buffer1);
    text_layer_set_font(user_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_18));
    text_layer_set_text_alignment(user_textbar_layer, GTextAlignmentCenter);
    
    user_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect user_sign_layer_bounds = layer_get_bounds(window_layer);
    user_sign_layer_bounds.size.h = textbar_height;
    user_sign_layer_bounds.size.w = textbar_height;
    user_sign_layer_bounds.origin.x = 144-textbar_height;
    user_downloading_sign_layer = layer_create(user_sign_layer_bounds);
    layer_set_update_proc(user_downloading_sign_layer, downloading_sign_layer_update);
    
    GRect text_bounds = layer_get_bounds(window_layer);
    text_bounds.size.h = text_layer_height;
    text_bounds.origin.y += textbar_height;
    user_text_layer = text_layer_create(text_bounds);
    static char text_buffer2[40];
    snprintf(text_buffer2,40," Points: %i\n Rank: %i",user.points,user.rank);
    text_layer_set_text(user_text_layer,text_buffer2);
    text_layer_set_font(user_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(user_text_layer, GTextAlignmentLeft);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height + text_layer_height;
    menu_bounds.origin.y += textbar_height + text_layer_height;
    achievements_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(achievements_menu_layer, NULL, achievements_menu_callbacks);
    menu_layer_set_click_config_onto_window(achievements_menu_layer, window);
    
    layer_add_child(window_layer, text_layer_get_layer(user_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(user_textbar_inverter_layer));
    layer_add_child(window_layer, user_downloading_sign_layer);
    layer_add_child(window_layer, text_layer_get_layer(user_text_layer));
    layer_add_child(window_layer, menu_layer_get_layer(achievements_menu_layer));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_window_load() end");
}

static void user_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_window_unload() start");
    menu_layer_destroy(achievements_menu_layer);
    text_layer_destroy(user_text_layer);
    layer_destroy(user_downloading_sign_layer);
    inverter_layer_destroy(user_textbar_inverter_layer);
    text_layer_destroy(user_textbar_layer);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "user_window_unload() end");
}
/////////////////////////////////////

///////////////////////////////////// BEACONS WINDOW
static uint16_t get_num_sections_beacons(MenuLayer *menu_layer, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_num_sections_beacons()");
    return 2;
}

static int16_t get_header_height_beacons(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_header_height_beacons()");
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

static int16_t get_cell_height_beacons(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "get_cell_height_beacons()");
    return 26;
}

static void draw_beacon_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "draw_beacon_header()");
    switch (section_index) {
        case 0:
            menu_cell_basic_header_draw(ctx, cell_layer, "Beacons in range");
            break;
        case 1:
            menu_cell_basic_header_draw(ctx, cell_layer, "Beacons out of range");
            break;
        default:
            menu_cell_basic_header_draw(ctx, cell_layer, "Looking for beacons...");
            break;
    }
}

static void draw_beacon_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "draw_beacon_row()");
    switch (cell_index->section) {
        case 0:
            if(beacons!=NULL)
                menu_cell_basic_draw(ctx, cell_layer, beacons[cell_index->row]->name, NULL, NULL);
            break;
        case 1:
            if(beacons!=NULL)
                menu_cell_basic_draw(ctx, cell_layer, beacons[num_beacons_in_range+cell_index->row]->name, NULL, NULL);
            break;
    }
}

static void beacon_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_select_click()");
    if(cell_index->section==0) {
        current_beacon = beacons[cell_index->row];
    }
    else if(cell_index->section==1) {
        current_beacon = beacons[num_beacons_in_range+cell_index->row];
    }
    APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_select_click() current_beacon: %s",current_beacon->name);
    
    window_stack_push(beacon_details_window, true);
}

MenuLayerCallbacks beacons_menu_callbacks = {
    .get_num_sections = get_num_sections_beacons,
    .get_num_rows = get_num_beacons,
    .get_header_height = get_header_height_beacons,
    .get_cell_height = get_cell_height_beacons,
    .draw_header = draw_beacon_header,
    .draw_row = draw_beacon_row,
    .select_click = beacon_select_click
};

static void beacons_window_load(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    beacons_textbar_layer = text_layer_create(textbar_bounds);
    text_layer_set_text(beacons_textbar_layer,user.name);
    text_layer_set_font(beacons_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_18));
    text_layer_set_text_alignment(beacons_textbar_layer, GTextAlignmentCenter);
    
    beacons_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect downloading_sign_layer_bounds = layer_get_bounds(window_layer);
    downloading_sign_layer_bounds.size.h = textbar_height;
    downloading_sign_layer_bounds.size.w = textbar_height;
    downloading_sign_layer_bounds.origin.x = 144-textbar_height;
    beacons_downloading_sign_layer = layer_create(downloading_sign_layer_bounds);
    layer_set_update_proc(beacons_downloading_sign_layer, downloading_sign_layer_update);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    beacons_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(beacons_menu_layer, NULL, beacons_menu_callbacks);
    menu_layer_set_click_config_onto_window(beacons_menu_layer, window);
    
    layer_add_child(window_layer, text_layer_get_layer(beacons_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(beacons_textbar_inverter_layer));
    layer_add_child(window_layer, beacons_downloading_sign_layer);
    layer_add_child(window_layer, menu_layer_get_layer(beacons_menu_layer));
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_load() end");
}

static void beacons_window_unload(Window *window) {
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_unload() start");
    menu_layer_destroy(beacons_menu_layer);
    layer_destroy(beacons_downloading_sign_layer);
    inverter_layer_destroy(beacons_textbar_inverter_layer);
    text_layer_destroy(beacons_textbar_layer);
    //APP_LOG(APP_LOG_LEVEL_DEBUG, "beacons_window_unload() start");
}
/////////////////////////////////////

///////////////////////////////////// BEACON DETAILS WINDOW
static void dinstance_layer_update(Layer *layer, GContext *ctx) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "dinstance_layer_update() start");
    int height = 34;
    int width = 136;
    graphics_context_set_stroke_color(ctx, GColorBlack);
    graphics_draw_round_rect(ctx,GRect(4,3,width,height),8);
    graphics_context_set_fill_color(ctx, GColorBlack);
    if(current_beacon->distance<96)
        graphics_fill_rect(ctx,GRect(4,3,width*current_beacon->distance/100.0,height),8,GCornersLeft);
    else if(current_beacon->distance>=96)
        graphics_fill_rect(ctx,GRect(4,3,width*current_beacon->distance/100.0,height),8,GCornersAll);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "dinstance_layer_update() end");
}

static void games_list_select_handler(ClickRecognizerRef recognizer, void *context) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "games_list_select_handler() start");
    send_simple_request(REQUEST_GAMES);
    is_downloading = true;
    window_stack_push(games_window, true);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "games_list_select_handler() end");
}

static void beacon_details_click_config_provider(void *context) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_details_click_config_provider() start");
    window_single_click_subscribe(BUTTON_ID_SELECT,(ClickHandler)games_list_select_handler);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_details_click_config_provider() end");
}

static void beacon_details_window_load(Window *window) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_details_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    int uppertext_layer_height = 30;
    int distance_layer_height = 40;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_details_window_load() 1");
    GRect textbar_bounds = layer_get_bounds(window_layer);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_details_window_load() 2");
    textbar_bounds.size.h = textbar_height;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "textbar_bounds.size.h = %u",textbar_bounds.size.h);
    beacon_details_textbar_layer = text_layer_create(textbar_bounds);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_details_window_load() start");
    text_layer_set_text(beacon_details_textbar_layer,current_beacon->name);
    text_layer_set_font(beacon_details_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_18));
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
    
    GRect lowertext_bounds = layer_get_bounds(window_layer);
    lowertext_bounds.size.h -= textbar_height + uppertext_layer_height + distance_layer_height;
    lowertext_bounds.origin.y = textbar_height + uppertext_layer_height + distance_layer_height;
    beacon_details_lowertext_layer = text_layer_create(lowertext_bounds);
    static char text_buffer2[70];
    snprintf(text_buffer2,70,"Active games: %i\nCompleted games: %i\n\nclick select for more...",current_beacon->games_active,current_beacon->games_completed);
    text_layer_set_text(beacon_details_lowertext_layer,text_buffer2);
    text_layer_set_font(beacon_details_lowertext_layer,fonts_get_system_font(FONT_KEY_GOTHIC_18));
    text_layer_set_text_alignment(beacon_details_lowertext_layer, GTextAlignmentLeft);
    
    layer_add_child(window_layer, text_layer_get_layer(beacon_details_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(beacon_details_textbar_inverter_layer));
    layer_add_child(window_layer, text_layer_get_layer(beacon_details_uppertext_layer));
    layer_add_child(window_layer, beacon_details_distance_layer);
    layer_add_child(window_layer, text_layer_get_layer(beacon_details_lowertext_layer));
    APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_details_window_load() end");
}

static void beacon_details_window_unload(Window *window) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_details_window_unload() start");
    text_layer_destroy(beacon_details_lowertext_layer);
    layer_destroy(beacon_details_distance_layer);
    text_layer_destroy(beacon_details_uppertext_layer);
    inverter_layer_destroy(beacon_details_textbar_inverter_layer);
    text_layer_destroy(beacon_details_textbar_layer);
    //current_beacon = NULL;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "beacon_details_window_unload() end");
}
/////////////////////////////////////

///////////////////////////////////// GAMES WINDOW
static uint16_t get_num_sections_games(MenuLayer *menu_layer, void *data) {
    return 2;
}

static int16_t get_header_height_games(MenuLayer *menu_layer, uint16_t section_index, void *data) {
    return MENU_CELL_BASIC_HEADER_HEIGHT;
}

static int16_t get_cell_height_games(MenuLayer *menu_layer, MenuIndex *cell_index, void *data) {
    return 26;
}

static void draw_game_header(GContext* ctx, const Layer *cell_layer, uint16_t section_index, void *data) {
    switch (section_index) {
        case 0:
            menu_cell_basic_header_draw(ctx, cell_layer, "Active games");
            break;
        case 1:
            menu_cell_basic_header_draw(ctx, cell_layer, "Completed games");
            break;
    }
}

static void draw_game_row(GContext *ctx, const Layer *cell_layer, MenuIndex *cell_index, void *callback_context) {
    switch (cell_index->section) {
        case 0:
            if(games!=NULL)
                menu_cell_basic_draw(ctx, cell_layer, games[num_games_completed+cell_index->row]->name, NULL, NULL);
            break;
        case 1:
            if(games!=NULL)
                menu_cell_basic_draw(ctx, cell_layer, games[cell_index->row]->name, NULL, NULL);
            break;
    }
}

static void game_select_click(struct MenuLayer *menu_layer, MenuIndex *cell_index, void *callback_context) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "game_select_click() start");
    if(cell_index->section==0) {
        current_game = games[num_games_completed+cell_index->row];
    }
    else if(cell_index->section==1) {
        current_game = games[cell_index->row];
    }
    
    if(current_game!=NULL)
        APP_LOG(APP_LOG_LEVEL_DEBUG, "game selected: %s",current_game->name);
    else
        APP_LOG(APP_LOG_LEVEL_DEBUG, "current_game==NULL!");
    
    window_stack_push(game_details_window, true);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "game_select_click() end");
}

MenuLayerCallbacks games_menu_callbacks = {
    .get_num_sections = get_num_sections_games,
    .get_num_rows = get_num_games,
    .get_header_height = get_header_height_games,
    .get_cell_height = get_cell_height_games,
    .draw_header = draw_game_header,
    .draw_row = draw_game_row,
    .select_click = game_select_click
};

static void games_window_load(Window *window) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "games_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    games_textbar_layer = text_layer_create(textbar_bounds);
    static char text_buffer[30];
    snprintf(text_buffer,30,"%s",current_beacon->name);
    text_layer_set_text(games_textbar_layer,text_buffer);
    text_layer_set_font(games_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_18));
    text_layer_set_text_alignment(games_textbar_layer, GTextAlignmentCenter);
    
    games_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect menu_bounds = layer_get_bounds(window_layer);
    menu_bounds.size.h -= textbar_height;
    menu_bounds.origin.y += textbar_height;
    games_menu_layer = menu_layer_create(menu_bounds);
    menu_layer_set_callbacks(games_menu_layer, NULL, games_menu_callbacks);
    menu_layer_set_click_config_onto_window(games_menu_layer, window);
    
    layer_add_child(window_layer, text_layer_get_layer(games_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(games_textbar_inverter_layer));
    layer_add_child(window_layer, menu_layer_get_layer(games_menu_layer));
    APP_LOG(APP_LOG_LEVEL_DEBUG, "games_window_load() end");
}

static void games_window_unload(Window *window) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "games_window_unload() start");
    menu_layer_destroy(games_menu_layer);
    inverter_layer_destroy(games_textbar_inverter_layer);
    text_layer_destroy(games_textbar_layer);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "games_window_unload() end");
}
/////////////////////////////////////

///////////////////////////////////// GAME DETAILS WINDOW
static void game_details_window_load(Window *window) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "game_details_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    game_details_textbar_layer = text_layer_create(textbar_bounds);
    text_layer_set_text(game_details_textbar_layer,current_game->name);
    text_layer_set_font(game_details_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_18));
    text_layer_set_text_alignment(game_details_textbar_layer, GTextAlignmentCenter);
    
    game_details_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect text_bounds = layer_get_bounds(window_layer);
    text_bounds.size.h -= textbar_height;
    text_bounds.origin.y = textbar_height;
    game_details_text_layer = text_layer_create(text_bounds);
    static char text_buffer[30];
    snprintf(text_buffer,30,"%s\n%i/%i",current_game->description,current_game->progress,current_game->goal);
    text_layer_set_text(game_details_text_layer,text_buffer);
    text_layer_set_font(game_details_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(game_details_text_layer, GTextAlignmentCenter);
    
    layer_add_child(window_layer, text_layer_get_layer(game_details_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(game_details_textbar_inverter_layer));
    layer_add_child(window_layer, text_layer_get_layer(game_details_text_layer));
    APP_LOG(APP_LOG_LEVEL_DEBUG, "game_details_window_load() end");
}

static void game_details_window_unload(Window *window) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "game_details_window_unload() start");
    text_layer_destroy(game_details_text_layer);
    inverter_layer_destroy(game_details_textbar_inverter_layer);
    text_layer_destroy(game_details_textbar_layer);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "game_details_window_unload() end");
}
/////////////////////////////////////

///////////////////////////////////// ACHIEVEMENT DETAILS WINDOW
static void achievement_details_window_load(Window *window) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_details_window_load() start");
    Layer *window_layer = window_get_root_layer(window);
    
    GRect textbar_bounds = layer_get_bounds(window_layer);
    textbar_bounds.size.h = textbar_height;
    achievement_details_textbar_layer = text_layer_create(textbar_bounds);
    static char text_buffer1[30];
    snprintf(text_buffer1,30,"%s",current_achievement->name);
    text_layer_set_text(achievement_details_textbar_layer,text_buffer1);
    text_layer_set_font(achievement_details_textbar_layer,fonts_get_system_font(FONT_KEY_GOTHIC_18));
    text_layer_set_text_alignment(achievement_details_textbar_layer, GTextAlignmentCenter);
    
    achievement_details_textbar_inverter_layer = inverter_layer_create(textbar_bounds);
    
    GRect text_bounds = layer_get_bounds(window_layer);
    text_bounds.size.h -= textbar_height;
    text_bounds.origin.y = textbar_height;
    achievement_details_text_layer = text_layer_create(text_bounds);
    text_layer_set_text(achievement_details_text_layer,current_achievement->description);
    text_layer_set_font(achievement_details_text_layer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
    text_layer_set_text_alignment(achievement_details_text_layer, GTextAlignmentCenter);
    
    layer_add_child(window_layer, text_layer_get_layer(achievement_details_textbar_layer));
    layer_add_child(window_layer, inverter_layer_get_layer(achievement_details_textbar_inverter_layer));
    layer_add_child(window_layer, text_layer_get_layer(achievement_details_text_layer));
    APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_details_window_load() end");
}

static void achievement_details_window_unload(Window *window) {
    APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_details_window_unload() start");
    text_layer_destroy(achievement_details_text_layer);
    inverter_layer_destroy(achievement_details_textbar_inverter_layer);
    text_layer_destroy(achievement_details_textbar_layer);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "achievement_details_window_unload() end");
}
/////////////////////////////////////

/////////////////////////////////////
static WindowHandlers login_window_handlers = {
    .load = login_window_load,
    .unload = login_window_unload,
    .appear = login_window_appear
};
static WindowHandlers user_window_handlers = {
    .load = user_window_load,
    .unload = user_window_unload
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
    .load = games_window_load,
    .unload = games_window_unload
};
static WindowHandlers game_details_window_handlers = {
    .load = game_details_window_load,
    .unload = game_details_window_unload
};
static WindowHandlers achievement_details_window_handlers = {
    .load = achievement_details_window_load,
    .unload = achievement_details_window_unload
};

static void init() {
    num_beacons = 0;
    num_beacons_in_range = 0;
    num_beacons_out_of_range = 0;
    num_games = 0;
    num_games_active = 0;
    num_games_completed = 0;
    num_achievements = 0;
    prev_num_achievements = 0;
    
    is_downloading = false;
    
    login_window = window_create();
    window_set_fullscreen(login_window, true);
    window_set_window_handlers(login_window, login_window_handlers);
    
    user_window = window_create();
    window_set_fullscreen(user_window, true);
    window_set_window_handlers(user_window, user_window_handlers);
    
    beacons_window = window_create();
    window_set_fullscreen(beacons_window, true);
    window_set_window_handlers(beacons_window, beacons_window_handlers);
    
    beacon_details_window = window_create();
    window_set_fullscreen(beacon_details_window, true);
    window_set_window_handlers(beacon_details_window, beacon_details_window_handlers);
    window_set_click_config_provider(beacon_details_window, beacon_details_click_config_provider);
    
    games_window = window_create();
    window_set_fullscreen(games_window, true);
    window_set_window_handlers(games_window, games_window_handlers);
    
    game_details_window = window_create();
    window_set_fullscreen(game_details_window, true);
    window_set_window_handlers(game_details_window, game_details_window_handlers);
    
    achievement_details_window = window_create();
    window_set_fullscreen(achievement_details_window, true);
    window_set_window_handlers(achievement_details_window, achievement_details_window_handlers);
    
    app_message_register_inbox_received(in_received_handler);
    app_message_register_inbox_dropped(in_dropped_handler);
    app_message_register_outbox_sent(out_sent_handler);
    app_message_register_outbox_failed(out_failed_handler);
    
    const int inbound_size = app_message_inbox_size_maximum();
    const int outbound_size = 128;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "App_message initialising: %s",translate_result(app_message_open(200,outbound_size)));
    APP_LOG(APP_LOG_LEVEL_DEBUG, "App_message inbound size: %u",inbound_size);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "App_message outbound size: %u",outbound_size);
    
    window_stack_push(login_window, true);
}

static void deinit() {
    window_destroy(achievement_details_window);
    window_destroy(game_details_window);
    window_destroy(games_window);
    window_destroy(beacon_details_window);
    window_destroy(beacons_window);
    window_destroy(user_window);
    window_destroy(login_window);
    
    if(user.name!=NULL)
        free(user.name);
    clear_beacons_table(beacons);
    clear_games_table(games);
    clear_achievements_table(achievements);
}
/////////////////////////////////////

int main(void) {
    init();
    app_event_loop();
    deinit();
}
